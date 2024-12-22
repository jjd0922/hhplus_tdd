package io.hhplus.tdd.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryTable pointHistoryTable = new PointHistoryTable();
    private final UserPointTable userPointTable = new UserPointTable();

    // 포인트 내역 insert
    public PointHistory pointHistoryInsert(PointHistory pointHistory){
        if(pointHistory == null){
            throw new IllegalArgumentException("Invalid data");
        }
        long point = this.selectById(pointHistory.userId()).point();
        if(point - pointHistory.amount() < 0 && pointHistory.type().equals(TransactionType.USE)){
            throw new IllegalArgumentException("보유하신 포인트가 부족합니다.");
        }
        if(point + pointHistory.amount() > 100000L && pointHistory.type().equals(TransactionType.CHARGE)){
            throw new IllegalArgumentException("최대잔고는 100,000 포인트까지 충전 가능합니다.");
        }
        return pointHistoryTable.insert(pointHistory.userId(), pointHistory.amount(), pointHistory.type(), pointHistory.updateMillis());
    }

    // 유저 전체 포인트 내역 조회
    public List<PointHistory> selectAllByUserId(long userId){
        return pointHistoryTable.selectAllByUserId(userId);
    }

    // 유저의 포인트 잔액 조회
    public UserPoint selectById(long id){
        return userPointTable.selectById(id);
    }

    // 유저포인트 update or insert
    public UserPoint insertOrUpdate(long id){
        long amount = 0L;
        if(this.selectAllByUserId(id).isEmpty()){
            throw new IllegalArgumentException("포인트 내역이 없습니다.");
        }
        for (PointHistory pointHistory : this.selectAllByUserId(id)) {
            if(pointHistory.type().equals(TransactionType.CHARGE)){
                amount += pointHistory.amount();
            }
            if(pointHistory.type().equals(TransactionType.USE)){
                amount -= pointHistory.amount();
               }
        }
        if(amount < 0){
            throw new IllegalArgumentException("포인트 잔액은 0보다 작을 수 없습니다.");
        }
        return userPointTable.insertOrUpdate(id,amount);
    }
}
