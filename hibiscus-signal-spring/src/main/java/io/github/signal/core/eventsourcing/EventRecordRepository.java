package io.github.signal.core.eventsourcing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 事件记录Repository
 * 提供事件记录的数据库操作接口
 */
@Repository
@ConditionalOnClass(JpaRepository.class)
public interface EventRecordRepository extends JpaRepository<EventRecord, Long> {

    /**
     * 根据事件ID查找事件记录
     */
    Optional<EventRecord> findByEventId(String eventId);

    /**
     * 根据聚合根ID查找事件记录，按版本排序
     */
    List<EventRecord> findByAggregateIdOrderByVersion(String aggregateId);

    /**
     * 根据聚合根ID和版本范围查找事件记录，按版本排序
     */
    List<EventRecord> findByAggregateIdAndVersionGreaterThanOrderByVersion(String aggregateId, Long fromVersion);

    /**
     * 根据事件类型查找事件记录，按发生时间排序
     */
    List<EventRecord> findByEventTypeOrderByOccurredAt(String eventType);

    /**
     * 根据时间范围查找事件记录，按发生时间排序
     */
    List<EventRecord> findByOccurredAtBetweenOrderByOccurredAt(LocalDateTime start, LocalDateTime end);

    /**
     * 查找所有事件记录，按发生时间排序
     */
    List<EventRecord> findAllByOrderByOccurredAt();

    /**
     * 检查聚合根是否存在
     */
    boolean existsByAggregateId(String aggregateId);

    /**
     * 根据聚合根ID删除所有事件记录
     */
    void deleteByAggregateId(String aggregateId);

    /**
     * 获取聚合根的当前版本
     */
    @Query("SELECT MAX(e.version) FROM EventRecord e WHERE e.aggregateId = :aggregateId")
    Long getCurrentVersion(@Param("aggregateId") String aggregateId);

    /**
     * 获取聚合根的事件数量
     */
    @Query("SELECT COUNT(e) FROM EventRecord e WHERE e.aggregateId = :aggregateId")
    Long countByAggregateId(@Param("aggregateId") String aggregateId);

    /**
     * 根据聚合根ID和版本查找事件记录
     */
    Optional<EventRecord> findByAggregateIdAndVersion(String aggregateId, Long version);

    /**
     * 根据事件名称查找事件记录
     */
    List<EventRecord> findByEventNameOrderByOccurredAt(String eventName);

    /**
     * 根据聚合根ID获取事件统计信息
     */
    @Query("SELECT e.eventType, COUNT(e) FROM EventRecord e WHERE e.aggregateId = :aggregateId GROUP BY e.eventType")
    List<Object[]> getEventTypeStatsByAggregateId(@Param("aggregateId") String aggregateId);

    /**
     * 获取指定时间范围内的事件统计
     */
    @Query("SELECT e.eventType, COUNT(e) FROM EventRecord e WHERE e.occurredAt BETWEEN :start AND :end GROUP BY e.eventType")
    List<Object[]> getEventTypeStatsByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
