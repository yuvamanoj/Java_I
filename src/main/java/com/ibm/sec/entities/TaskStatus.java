package com.ibm.sec.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "task_statuses")
@Getter
@Setter
@NoArgsConstructor
public class TaskStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "created_date_time", nullable = false)
    private Date createdDateTime;

    @Column(name = "initiated_date_time", nullable = false)
    private Date initiatedDateTime;

    @Column(name = "updated_date_time", nullable = false)
    private Date updatedDateTime;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @Column(name = "status_name")
    private String statusName;

    @Column(name = "retry")
    private int retry;

    @Column(name = "error")
    private String error;

    public TaskStatus(String userId, Long taskId, String taskName, Date createdDateTime, Date initiatedDateTime, Date updatedDateTime, Long statusId, String statusName, int retry, String error){
        this.userId = userId;
        this.taskId = taskId;
        this.taskName = taskName;
        this.createdDateTime = createdDateTime;
        this.initiatedDateTime = initiatedDateTime;
        this.updatedDateTime = updatedDateTime;
        this.statusId = statusId;
        this.statusName = statusName;
        this.retry = retry;
        this.error = error;
    }
}
