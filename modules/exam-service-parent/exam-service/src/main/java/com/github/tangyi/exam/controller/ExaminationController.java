package com.github.tangyi.exam.controller;

import com.github.pagehelper.PageInfo;
import com.github.tangyi.common.core.constant.CommonConstant;
import com.github.tangyi.common.core.model.ResponseBean;
import com.github.tangyi.common.core.utils.PageUtil;
import com.github.tangyi.common.core.utils.SysUtil;
import com.github.tangyi.common.core.web.BaseController;
import com.github.tangyi.common.log.annotation.Log;
import com.github.tangyi.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.tangyi.exam.api.dto.ExaminationDto;
import com.github.tangyi.exam.api.dto.SubjectDto;
import com.github.tangyi.exam.api.module.Course;
import com.github.tangyi.exam.api.module.Examination;
import com.github.tangyi.exam.api.module.ExaminationSubject;
import com.github.tangyi.exam.service.CourseService;
import com.github.tangyi.exam.service.ExaminationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试controller
 *
 * @author tangyi
 * @date 2018/11/8 21:26
 */
@Slf4j
@AllArgsConstructor
@Api("考试信息管理")
@RestController
@RequestMapping("/v1/examination")
public class ExaminationController extends BaseController {

    private final ExaminationService examinationService;

    private final CourseService courseService;

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author tangyi
     * @date 2018/11/10 21:08
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取考试信息", notes = "根据考试id获取考试详细信息")
    @ApiImplicitParam(name = "id", value = "考试ID", required = true, dataType = "String", paramType = "path")
    public ResponseBean<Examination> examination(@PathVariable Long id) {
        Examination examination = new Examination();
        examination.setId(id);
        return new ResponseBean<>(examinationService.get(examination));
    }

    /**
     * 获取分页数据
     *
     * @param pageNum     pageNum
     * @param pageSize    pageSize
     * @param sort        sort
     * @param order       order
     * @param examination examination
     * @return PageInfo
     * @author tangyi
     * @date 2018/11/10 21:10
     */
    @GetMapping("examinationList")
    @ApiOperation(value = "获取考试列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "examination", value = "考试信息", dataType = "Examination")
    })
    public PageInfo<ExaminationDto> examinationList(@RequestParam(value = CommonConstant.PAGE_NUM, required = false, defaultValue = CommonConstant.PAGE_NUM_DEFAULT) String pageNum,
                                                    @RequestParam(value = CommonConstant.PAGE_SIZE, required = false, defaultValue = CommonConstant.PAGE_SIZE_DEFAULT) String pageSize,
                                                    @RequestParam(value = CommonConstant.SORT, required = false, defaultValue = CommonConstant.PAGE_SORT_DEFAULT) String sort,
                                                    @RequestParam(value = CommonConstant.ORDER, required = false, defaultValue = CommonConstant.PAGE_ORDER_DEFAULT) String order,
                                                    Examination examination) {
        examination.setTenantCode(SysUtil.getTenantCode());
        PageInfo<Examination> page = examinationService.findPage(PageUtil.pageInfo(pageNum, pageSize, sort, order), examination);
        PageInfo<ExaminationDto> examinationDtoPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(page, examinationDtoPageInfo);
        if (CollectionUtils.isNotEmpty(page.getList())) {
            List<Course> courses = courseService.findListById(page.getList().stream().map(Examination::getCourseId).distinct().toArray(Long[]::new));
            List<ExaminationDto> examinationDtos = page.getList().stream().map(exam -> {
                ExaminationDto examinationDto = new ExaminationDto();
                BeanUtils.copyProperties(exam, examinationDto);
                // 设置考试所属课程
                courses.stream().filter(tempCourse -> tempCourse.getId().equals(exam.getCourseId())).findFirst().ifPresent(examinationDto::setCourse);
                return examinationDto;
            }).collect(Collectors.toList());
            examinationDtoPageInfo.setList(examinationDtos);
        }
        return examinationDtoPageInfo;
    }

    /**
     * 根据考试ID获取题目分页数据
     *
     * @param pageNum    pageNum
     * @param pageSize   pageSize
     * @param sort       sort
     * @param order      order
     * @param subjectDto subjectDto
     * @return PageInfo
     * @author tangyi
     * @date 2019/6/16 15:45
     */
    @RequestMapping("/{examinationId}/subjectList")
    @ApiOperation(value = "获取题目列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "examinationId", value = "考试ID", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "subjectDto", value = "题目信息", dataType = "SubjectDto")
    })
    public PageInfo<SubjectDto> subjectList(@RequestParam(value = CommonConstant.PAGE_NUM, required = false, defaultValue = CommonConstant.PAGE_NUM_DEFAULT) String pageNum,
                                            @RequestParam(value = CommonConstant.PAGE_SIZE, required = false, defaultValue = CommonConstant.PAGE_SIZE_DEFAULT) String pageSize,
                                            @RequestParam(value = CommonConstant.SORT, required = false, defaultValue = CommonConstant.PAGE_SORT_DEFAULT) String sort,
                                            @RequestParam(value = CommonConstant.ORDER, required = false, defaultValue = CommonConstant.PAGE_ORDER_DEFAULT) String order,
                                            @PathVariable Long examinationId, SubjectDto subjectDto) {
        subjectDto.setExaminationId(examinationId);
        return examinationService.findSubjectPageById(subjectDto, pageNum, pageSize, sort, order);
    }


    /**
     * 创建
     *
     * @param examinationDto examinationDto
     * @return ResponseBean
     * @author tangyi
     * @date 2018/11/10 21:14
     */
    @PostMapping
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "创建考试", notes = "创建考试")
    @ApiImplicitParam(name = "examinationDto", value = "考试实体examinationDto", required = true, dataType = "ExaminationDto")
    @Log("新增考试")
    public ResponseBean<Boolean> addExamination(@RequestBody @Valid ExaminationDto examinationDto) {
        Examination examination = new Examination();
        BeanUtils.copyProperties(examinationDto, examination);
        examination.setCourseId(examinationDto.getCourse().getId());
        examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), SysUtil.getTenantCode());
        return new ResponseBean<>(examinationService.insert(examination) > 0);
    }

    /**
     * 更新
     *
     * @param examinationDto examinationDto
     * @return ResponseBean
     * @author tangyi
     * @date 2018/11/10 21:15
     */
    @PutMapping
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "更新考试信息", notes = "根据考试id更新考试的基本信息")
    @ApiImplicitParam(name = "examinationDto", value = "考试实体answer", required = true, dataType = "ExaminationDto")
    @Log("更新考试")
    public ResponseBean<Boolean> updateExamination(@RequestBody @Valid ExaminationDto examinationDto) {
        Examination examination = new Examination();
        BeanUtils.copyProperties(examinationDto, examination);
        if (examinationDto.getCourse() != null)
            examination.setCourseId(examinationDto.getCourse().getId());
        examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), SysUtil.getTenantCode());
        return new ResponseBean<>(examinationService.update(examination) > 0);
    }

    /**
     * 删除考试
     *
     * @param id id
     * @return ResponseBean
     * @author tangyi
     * @date 2018/11/10 21:20
     */
    @DeleteMapping("{id}")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "删除考试", notes = "根据ID删除考试")
    @ApiImplicitParam(name = "id", value = "考试ID", required = true, paramType = "path")
    @Log("删除考试")
    public ResponseBean<Boolean> deleteExamination(@PathVariable Long id) {
        boolean success = false;
        try {
            Examination examination = new Examination();
            examination.setId(id);
            examination = examinationService.get(examination);
            if (examination != null) {
                examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), SysUtil.getTenantCode());
                success = examinationService.delete(examination) > 0;
            }
        } catch (Exception e) {
            log.error("删除考试失败！", e);
        }
        return new ResponseBean<>(success);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author tangyi
     * @date 2018/12/03 22:03
     */
    @PostMapping("deleteAll")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "批量删除考试", notes = "根据考试id批量删除考试")
    @ApiImplicitParam(name = "ids", value = "考试ID", dataType = "Long")
    @Log("批量删除考试")
    public ResponseBean<Boolean> deleteAllExaminations(@RequestBody Long[] ids) {
        boolean success = false;
        try {
            if (ArrayUtils.isNotEmpty(ids))
                success = examinationService.deleteAll(ids) > 0;
        } catch (Exception e) {
            log.error("删除考试失败！", e);
        }
        return new ResponseBean<>(success);
    }

    /**
     * 查询考试数量
     *
     * @param tenantCode 租户标识
     * @return ResponseBean
     * @author tangyi
     * @date 2019/3/1 15:30
     */
    @GetMapping("examinationCount")
    public ResponseBean<Integer> findExaminationCount(@RequestParam @NotBlank String tenantCode) {
        Examination examination = new Examination();
        examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), tenantCode);
        return new ResponseBean<>(examinationService.findExaminationCount(examination));
    }

    /**
     * 根据考试ID查询题目id列表
     *
     * @param examinationId examinationId
     * @return ResponseBean
     * @author tangyi
     * @date 2019/06/18 14:31
     */
    @ApiImplicitParam(name = "examinationId", value = "考试ID", required = true, paramType = "path")
    @GetMapping("/{examinationId}/subjectIds")
    public ResponseBean<List<ExaminationSubject>> findExaminationSubjectIds(@PathVariable Long examinationId) {
        Examination examination = new Examination();
        examination.setId(examinationId);
        return new ResponseBean<>(examinationService.findListByExaminationId(examination));
    }

    /**
     * 查询参与考试人数
     *
     * @param tenantCode tenantCode
     * @return ResponseBean
     * @author tangyi
     * @date 2019/10/27 20:07:38
     */
    @GetMapping("examUserCount")
    public ResponseBean<Integer> findExamUserCount(@RequestParam @NotBlank String tenantCode) {
        Examination examination = new Examination();
        examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), tenantCode);
        return new ResponseBean<>(examinationService.findExamUserCount(examination));
    }
}
