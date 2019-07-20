package com.github.tangyi.exam.api.dto;

import com.github.tangyi.common.core.persistence.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 考试记录DTO
 *
 * @author tangyi
 * @date 2018-12-26 16:26
 */
@Data
public class ExaminationRecordDto extends BaseEntity<ExaminationRecordDto> {

    /**
     * 考生ID
     */
    private String userId;

    /**
     * 考试ID
     */
    private String examinationId;

    /**
     * 考试名称
     */
    private String examinationName;

    /**
     * 考试类型
     */
    private String type;

    /**
     * 考试注意事项
     */
    private String attention;

    /**
     * 当前时间
     */
    private Date currentTime;

    /**
     * 考试开始时间
     */
    private Date startTime;

    /**
     * 考试结束时间
     */
    private Date endTime;

    /**
     * 考试持续时间
     */
    private String duration;

    /**
     * 总分
     */
    private String totalScore;

    /**
     * 分数
     */
    private Integer score;

    /**
     * 考试状态
     */
    private Integer status;

    /**
     * 封面
     */
    private String avatar;

    /**
     * 学院
     */
    private String collegeId;

    /**
     * 专业
     */
    private String majorId;

    /**
     * 课程
     */
    private String courseId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 考生名称
     */
    private String userName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 考试时间
     */
    private String examTime;

    /**
     * 错误题目数量
     */
    private Integer inCorrectNumber;

    /**
     * 正确题目数量
     */
    private Integer correctNumber;

    /**
     * 提交状态 1-已提交 0-未提交
     */
    private Integer submitStatus;
}
