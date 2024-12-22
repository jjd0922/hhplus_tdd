package io.hhplus.tdd;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointService sut;

    @Test
    void pointHistoryTable_insert_whenDataExists(){

        // Given
        PointHistory pointHistory = new PointHistory(1L,1L,1000L, TransactionType.CHARGE,1000L);

        // When
        PointHistory resultPointHistory = sut.pointHistoryInsert(pointHistory);

        // Then
        Assertions.assertNotNull(resultPointHistory);
        Assertions.assertEquals(1L,resultPointHistory.userId());
        Assertions.assertEquals(1000L,resultPointHistory.amount());
        Assertions.assertEquals(TransactionType.CHARGE,resultPointHistory.type());
    }

    @Test
    void pointHistoryTable_ThrowException_whenDataIsNull(){

        // Given
        PointHistory pointHistory = null;

        // When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.pointHistoryInsert(pointHistory);
        }, "PointHistory must be not null");
    }

    @Test
    void pointHistoryTable_ThrowException_whenUserPointNotEnough(){

        // Given
        PointHistory pointHistory = new PointHistory(1L,1L,1000L, TransactionType.CHARGE,1000L);
        sut.pointHistoryInsert(pointHistory);
        sut.insertOrUpdate(pointHistory.userId());

        // When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.pointHistoryInsert(new PointHistory(2L,1L,2000L, TransactionType.USE,1000L));
        }, "포인트 부족");

    }

    @Test
    void pointHistoryTable_ThrowException_whenUserPointMaxBalance(){

        // Given
        PointHistory pointHistory = new PointHistory(1L,1L,99000L, TransactionType.CHARGE,1000L);
        sut.pointHistoryInsert(pointHistory);
        sut.insertOrUpdate(pointHistory.userId());

        // When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.pointHistoryInsert(new PointHistory(2L,1L,2000L, TransactionType.CHARGE,1000L));
        }, "최대잔고 이상으로 충전 불가");

    }

    @Test
    void pointHistoryTable_selectAllByUserId(){
        // Given
        sut.pointHistoryInsert(new PointHistory(1L,1L,1000L, TransactionType.CHARGE,1000L));
        sut.pointHistoryInsert(new PointHistory(2L,1L,2000L, TransactionType.CHARGE,1000L));

        // When
        List<PointHistory> pointHistoryList = sut.selectAllByUserId(1L);

        // Then
        Assertions.assertNotNull(pointHistoryList);
        Assertions.assertEquals(2,pointHistoryList.size());
        Assertions.assertEquals(1L,pointHistoryList.get(0).userId());
        Assertions.assertEquals(1000L,pointHistoryList.get(0).amount());
        Assertions.assertEquals( TransactionType.CHARGE,pointHistoryList.get(0).type());
        Assertions.assertEquals(1L,pointHistoryList.get(1).userId());
        Assertions.assertEquals(2000L,pointHistoryList.get(1).amount());
        Assertions.assertEquals( TransactionType.CHARGE,pointHistoryList.get(1).type());

    }

    @Test
    void pointHistoryTable_selectAllByUserId_whenDataNotExist(){

        // Given
        List<PointHistory> pointHistoryList = sut.selectAllByUserId(1L);

        // Then
        Assertions.assertTrue(pointHistoryList.isEmpty());

    }

    @Test
    void userPoimtTable_selectById_whenDataExists(){

        // Given
        sut.insertOrUpdate(1L);

        // When
        UserPoint userPoint = sut.selectById(1L);

        // Then
        Assertions.assertNotNull(userPoint);
        Assertions.assertEquals(1L,userPoint.id());
        Assertions.assertEquals(1000L,userPoint.point());

    }

    @Test
    void userPoimtTable_selectById_whenDataNotExists(){

        // When
        UserPoint userPoint = sut.selectById(1L);

        // Then
        Assertions.assertEquals(0, userPoint.point());

    }

    @Test
    void userPoimtTable_insertOrUpdate_whenDataExists(){

        // Given
        sut.pointHistoryInsert(new PointHistory(1L,1L,1000L, TransactionType.CHARGE,1000L));
        sut.pointHistoryInsert(new PointHistory(1L,1L,2000L, TransactionType.CHARGE,1000L));

        // When
        sut.insertOrUpdate(1L);
        UserPoint userPoint = sut.selectById(1L);

        // Then
        Assertions.assertNotNull(userPoint);
        Assertions.assertEquals(1L,userPoint.id());
        Assertions.assertEquals(3000L,userPoint.point());

    }

    @Test
    void userPoimtTable_insertOrUpdate_whenPointHistoryIsEmpty(){

        // Given
        long userId = 1L;

        // When Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.insertOrUpdate(userId);
        }, "포인트 내역이 없습니다.");

    }

    @Test
    void insertPoints_shouldThrowException_whenPointIsNegative() {
        // Given
        sut.pointHistoryInsert(new PointHistory(1L,1L,1000L, TransactionType.CHARGE,1000L));
        sut.pointHistoryInsert(new PointHistory(1L,1L,2000L, TransactionType.USE,1000L));

        // When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.insertOrUpdate(1L);
        }, "마이너스 잔액입니다.");
    }

}


