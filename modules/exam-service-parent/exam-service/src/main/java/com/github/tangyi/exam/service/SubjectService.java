package com.github.tangyi.exam.service;

import com.github.pagehelper.PageInfo;
import com.github.tangyi.common.core.exceptions.CommonException;
import com.github.tangyi.common.core.utils.PageUtil;
import com.github.tangyi.common.core.utils.SysUtil;
import com.github.tangyi.exam.api.constants.ExamSubjectConstant;
import com.github.tangyi.exam.api.dto.SubjectDto;
import com.github.tangyi.exam.api.module.ExaminationSubject;
import com.github.tangyi.exam.api.module.SubjectChoices;
import com.github.tangyi.exam.api.module.SubjectShortAnswer;
import com.github.tangyi.exam.enums.SubjectTypeEnum;
import com.github.tangyi.exam.utils.SubjectUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目service
 *
 * @author tangyi
 * @date 2019/6/16 17:22
 */
@AllArgsConstructor
@Slf4j
@Service
public class SubjectService {

    private final SubjectChoicesService subjectChoicesService;

    private final SubjectShortAnswerService subjectShortAnswerService;

    private final ExaminationSubjectService examinationSubjectService;

    private final SubjectOptionService subjectOptionService;

    private final SubjectJudgementService subjectJudgementService;

    /**
     * 根据题目ID，题目类型查询题目信息
     *
     * @param id   id
     * @param type type
     * @return SubjectDto
     * @author tangyi
     * @date 2019/06/16 17:24
     */
    public SubjectDto get(Long id, Integer type) {
        return subjectService(type).getSubject(id);
    }

    /**
     * 根据题目ID查询题目信息
     *
     * @param id id
     * @return SubjectDto
     * @author tangyi
     * @date 2019/06/16 17:26
     */
    public SubjectDto get(Long id) {
        Integer type = SubjectTypeEnum.CHOICES.getValue();
        ExaminationSubject es = new ExaminationSubject();
        es.setSubjectId(id);
        List<ExaminationSubject> examinationSubjects = examinationSubjectService.findListBySubjectId(es);
        if (CollectionUtils.isNotEmpty(examinationSubjects)) {
            type = examinationSubjects.get(0).getType();
        }
        return subjectService(type).getSubject(id);
    }

    /**
     * 根据上一题ID查找
     *
     * @param examinationId     examinationId
     * @param previousSubjectId previousSubjectId
     * @param type              type
     * @param nextType          0：下一题，1：上一题
     * @return SubjectDto
     * @author tangyi
     * @date 2019/06/18 13:49
     */
    @Transactional
    public SubjectDto getNextByCurrentIdAndType(Long examinationId, Long previousSubjectId, Integer type, Integer nextType) {
        return subjectService(type).getNextByCurrentIdAndType(examinationId, previousSubjectId, nextType);
    }

    /**
     * 查询列表
     *
     * @param subjectDto subjectDto
     * @return SubjectDto
     * @author tangyi
     * @date 2019/06/16 18:12
     */
    public List<SubjectDto> findList(SubjectDto subjectDto) {
        // 先查询题目考试关联表
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setExaminationId(subjectDto.getExaminationId());
        List<ExaminationSubject> examinationSubjects = examinationSubjectService.findList(examinationSubject);
        return this.findSubjectDtoList(examinationSubjects, true);
    }

    /**
     * 查询具体类型的题目列表
     *
     * @param subjectDto subjectDto
     * @return SubjectDto
     * @author tangyi
     * @date 2019/06/17 17:12
     */
    public List<SubjectDto> findListByType(SubjectDto subjectDto) {
        List<SubjectDto> subjectDtos = subjectService(subjectDto.getType()).findSubjectList(subjectDto);
        // 选择题则查找具体的选项
        if (SubjectTypeEnum.CHOICES.getValue().equals(subjectDto.getType())) {
            if (CollectionUtils.isNotEmpty(subjectDtos)) {
                // 查找选项信息
                subjectDtos = subjectDtos.stream().map(dto -> {
                    SubjectChoices subjectChoices = new SubjectChoices();
                    subjectChoices.setId(dto.getId());
                    subjectChoices = subjectChoicesService.get(subjectChoices);
                    return SubjectUtil.subjectChoicesToDto(subjectChoices);
                }).collect(Collectors.toList());
            }
        }
        return subjectDtos;
    }

    /**
     * 查询分页列表
     *
     * @param pageInfo   pageInfo
     * @param subjectDto subjectDto
     * @return SubjectDto
     * @author tangyi
     * @date 2019/06/16 18:12
     */
    public PageInfo<SubjectDto> findPage(PageInfo pageInfo, SubjectDto subjectDto) {
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setCategoryId(subjectDto.getCategoryId());
        examinationSubject.setExaminationId(subjectDto.getExaminationId());
        PageInfo<ExaminationSubject> examinationSubjectPageInfo = examinationSubjectService.findPage(pageInfo, examinationSubject);
        List<SubjectDto> subjectDtos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(examinationSubjectPageInfo.getList())) {
            examinationSubjectPageInfo.getList().forEach(tempExaminationSubject -> {
                SubjectDto tempSubjectDto = subjectService(tempExaminationSubject.getType()).getSubject(tempExaminationSubject.getSubjectId());
                if (tempSubjectDto != null)
                    subjectDtos.add(tempSubjectDto);
            });
        }
        PageInfo<SubjectDto> subjectDtoPageInfo = new PageInfo<>();
        PageUtil.copyProperties(examinationSubjectPageInfo, subjectDtoPageInfo);
        subjectDtoPageInfo.setList(subjectDtos);
        return subjectDtoPageInfo;
    }

    /**
     * 查询列表
     *
     * @param type type
     * @param ids  ids
     * @return SubjectDto
     * @author tangyi
     * @date 2019/06/16 18:14
     */
    public List<SubjectDto> findListById(Integer type, Long[] ids) {
        return subjectService(type).findSubjectListById(ids);
    }

    /**
     * 保存
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019/06/16 17:59
     */
    @Transactional
    public int insert(SubjectDto subjectDto) {
        // 保存与考试的关联关系
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setCommonValue(subjectDto.getCreator(), subjectDto.getApplicationCode(),
                subjectDto.getTenantCode());
        examinationSubject.setExaminationId(subjectDto.getExaminationId());
        examinationSubject.setCategoryId(subjectDto.getCategoryId());
        examinationSubject.setSubjectId(subjectDto.getId());
        examinationSubject.setType(subjectDto.getType());
        examinationSubjectService.insert(examinationSubject);
        // 保存选项
        if (CollectionUtils.isNotEmpty(subjectDto.getOptions())) {
            // 初始化基本属性
            subjectDto.getOptions().forEach(subjectOption -> {
                subjectOption.setCommonValue(subjectDto.getCreator(), subjectDto.getApplicationCode(),
                        subjectDto.getTenantCode());
                subjectOption.setSubjectChoicesId(subjectDto.getId());
            });
            // 批量保存
            subjectOptionService.insertBatch(subjectDto.getOptions());
        }
        return subjectService(subjectDto.getType()).insertSubject(subjectDto);
    }

    /**
     * 更新
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019/06/16 18:01
     */
    @Transactional
    public int update(SubjectDto subjectDto) {
        return subjectService(subjectDto.getType()).updateSubject(subjectDto);
    }

    /**
     * 删除
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019/06/16 18:04
     */
    @Transactional
    public int delete(SubjectDto subjectDto) {
        return subjectService(subjectDto.getType()).deleteSubject(subjectDto);
    }

    /**
     * 物理删除
     *
     * @param subjectDto subjectDto
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:51
     */
    @Transactional
    public int physicalDelete(SubjectDto subjectDto) {
        return subjectService(subjectDto.getType()).physicalDeleteSubject(subjectDto);
    }

    /**
     * 批量删除
     *
     * @param type type
     * @param ids  ids
     * @return int
     * @author tangyi
     * @date 2019/06/16 18:04
     */
    @Transactional
    public int deleteAll(Integer type, Long[] ids) {
        return subjectService(type).deleteAllSubject(ids);
    }

    /**
     * 物理批量删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:52
     */
    @Transactional
    public int physicalDeleteAll(Long[] ids) {
        if (ArrayUtils.isNotEmpty(ids)) {
            ExaminationSubject examinationSubject = new ExaminationSubject();
            SubjectDto subjectDto = new SubjectDto();
            for (Long id : ids) {
                examinationSubject.setSubjectId(id);
                List<ExaminationSubject> examinationSubjects = examinationSubjectService.findListBySubjectId(examinationSubject);
                if (CollectionUtils.isNotEmpty(examinationSubjects)) {
                    examinationSubjects.forEach(tempExaminationSubject -> {
                        subjectDto.setId(tempExaminationSubject.getSubjectId());
                        subjectService(tempExaminationSubject.getType()).physicalDeleteSubject(subjectDto);
                        examinationSubjectService.delete(tempExaminationSubject);
                    });
                }
            }
        }
        return 1;
    }

    /**
     * 根据题目类型返回对应的BaseSubjectService
     *
     * @param type type
     * @return BaseSubjectService
     * @author tangyi
     * @date 2019/06/16 17:34
     */
    private BaseSubjectService subjectService(Integer type) {
        BaseSubjectService baseSubjectService = this.subjectChoicesService;
        // 匹配类型
        SubjectTypeEnum subjectType = SubjectTypeEnum.match(type);
        if (subjectType != null) {
            switch (subjectType) {
                case CHOICES:
                case MULTIPLE_CHOICES:
                    baseSubjectService = this.subjectChoicesService;
                    break;
                case SHORT_ANSWER:
                    baseSubjectService = this.subjectShortAnswerService;
                    break;
                case JUDGEMENT:
                    baseSubjectService = this.subjectJudgementService;
                    break;
            }
        }
        return baseSubjectService;
    }

    /**
     * 导入题目
     *
     * @param subjects      subjects
     * @param examinationId examinationId
     * @param categoryId    categoryId
     * @return int
     * @author tangyi
     * @date 2019/06/17 14:39
     */
    @Transactional
    public int importSubject(List<SubjectDto> subjects, Long examinationId, Long categoryId) {
        int updated = 0;
        String creator = SysUtil.getUser(), sysCode = SysUtil.getSysCode(), tenantCode = SysUtil.getTenantCode();
        // 暂时循环遍历保存
        for (SubjectDto subject : subjects) {
            if (examinationId != null)
                subject.setExaminationId(examinationId);
            if (categoryId == null)
                categoryId = ExamSubjectConstant.DEFAULT_CATEGORY_ID;
            subject.setCategoryId(categoryId);
            if (subject.getId() == null) {
                subject.setCommonValue(creator, sysCode, tenantCode);
                updated += this.insert(subject);
            } else {
                subject.setCommonValue(creator, sysCode, tenantCode);
                updated += this.update(subject);
            }
        }
        return updated;
    }

    /**
     * 遍历关系集合，按类型分组题目ID，返回map
     *
     * @param examinationSubjects examinationSubjects
     * @return Map
     * @author tangyi
     * @date 2019/06/17 10:43
     */
    public Map<String, Long[]> getSubjectIdByType(List<ExaminationSubject> examinationSubjects) {
        Map<String, Long[]> idMap = new HashMap<>();
        examinationSubjects.stream().collect(Collectors.groupingBy(ExaminationSubject::getType, Collectors.toList()))
                .forEach((type, temp) -> {
                    // 匹配类型
                    SubjectTypeEnum subjectType = SubjectTypeEnum.match(type);
                    if (subjectType != null) {
                        switch (subjectType) {
                            case CHOICES:
                                idMap.put(SubjectTypeEnum.CHOICES.name(),
                                        temp.stream().map(ExaminationSubject::getSubjectId).distinct()
                                                .toArray(Long[]::new));
                                break;
                            case SHORT_ANSWER:
                                idMap.put(SubjectTypeEnum.SHORT_ANSWER.name(),
                                        temp.stream().map(ExaminationSubject::getSubjectId).distinct()
                                                .toArray(Long[]::new));
                                break;
                        }
                    }
                });
        return idMap;
    }

    /**
     * 根据关系列表查询对应的题目的详细信息
     *
     * @param examinationSubjects examinationSubjects
     * @return List
     * @author tangyi
     * @date 2019/06/17 10:54
     */
    public List<SubjectDto> findSubjectDtoList(List<ExaminationSubject> examinationSubjects) {
        return findSubjectDtoList(examinationSubjects, false);
    }

    /**
     * 根据关系列表查询对应的题目的详细信息
     *
     * @param examinationSubjects examinationSubjects
     * @return List
     * @author tangyi
     * @date 2019/06/17 11:54
     */
    public List<SubjectDto> findSubjectDtoList(List<ExaminationSubject> examinationSubjects, boolean findOptions) {
        Map<String, Long[]> idMap = this.getSubjectIdByType(examinationSubjects);
        // 查询题目信息，聚合
        List<SubjectDto> subjectDtoList = new ArrayList<>();
        if (idMap.containsKey(SubjectTypeEnum.CHOICES.name())) {
            List<SubjectChoices> subjectChoicesList = subjectChoicesService.findListById(idMap.get(SubjectTypeEnum.CHOICES.name()));
            if (CollectionUtils.isNotEmpty(subjectChoicesList)) {
                // 查找选项信息
                if (findOptions) {
                    subjectChoicesList = subjectChoicesList.stream().map(subjectChoicesService::get)
                            .collect(Collectors.toList());
                }
                subjectDtoList.addAll(SubjectUtil.subjectChoicesToDto(subjectChoicesList));
            }
        }
        if (idMap.containsKey(SubjectTypeEnum.SHORT_ANSWER.name())) {
            List<SubjectShortAnswer> subjectShortAnswers = subjectShortAnswerService.findListById(idMap.get(SubjectTypeEnum.SHORT_ANSWER.name()));
            if (CollectionUtils.isNotEmpty(subjectShortAnswers)) {
                subjectDtoList.addAll(SubjectUtil.subjectShortAnswerToDto(subjectShortAnswers));
            }
        }
        return subjectDtoList;
    }

    /**
     * 查询第一题
     *
     * @param examinationId examinationId
     * @return SubjectDto
     * @author tangyi
     * @date 2019/10/13 18:36:58
     */
    public SubjectDto findFirstSubjectByExaminationId(Long examinationId) {
        // 第一题
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setExaminationId(examinationId);
        // 根据考试ID查询考试题目管理关系，题目ID递增
        List<ExaminationSubject> examinationSubjects = examinationSubjectService.findList(examinationSubject);
        if (CollectionUtils.isEmpty(examinationSubjects))
            throw new CommonException("该考试未录入题目");
        // 第一题的ID
        examinationSubject = examinationSubjects.get(0);
        // 根据题目ID，类型获取题目的详细信息
        return this.get(examinationSubject.getSubjectId(), examinationSubject.getType());
    }

    /**
     * 导出
     *
     * @param ids           ids
     * @param examinationId examinationId
     * @param categoryId    categoryId
     * @return List
     */
    public List<SubjectDto> export(Long[] ids, Long examinationId, Long categoryId) {
        List<SubjectDto> subjects = new ArrayList<>();
        ExaminationSubject examinationSubject = new ExaminationSubject();
        List<ExaminationSubject> examinationSubjects = new ArrayList<>();
        // 根据题目id导出
        if (ArrayUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                examinationSubject.setSubjectId(id);
                examinationSubjects.addAll(examinationSubjectService.findListBySubjectId(examinationSubject));
            }
        } else if (examinationId != null) {
            // 根据考试ID
            examinationSubjects = examinationSubjectService.findListByExaminationId(examinationId);
        } else if (categoryId != null) {
            // 根据分类ID、类型导出
            examinationSubject.setCategoryId(categoryId);
            examinationSubjects = examinationSubjectService.findListByCategoryId(examinationSubject);
        }
        if (CollectionUtils.isNotEmpty(examinationSubjects)) {
            for (ExaminationSubject es : examinationSubjects)
                subjects.add(this.get(es.getSubjectId(), es.getType()));
        }
        return subjects;
    }
}
