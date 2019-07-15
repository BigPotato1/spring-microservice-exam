package com.github.tangyi.user.service;

import com.github.tangyi.common.core.exceptions.CommonException;
import com.github.tangyi.common.core.service.CrudService;
import com.github.tangyi.common.core.utils.SysUtil;
import com.github.tangyi.common.core.vo.UserVo;
import com.github.tangyi.user.api.dto.StudentDto;
import com.github.tangyi.user.api.module.Student;
import com.github.tangyi.user.api.module.UserStudent;
import com.github.tangyi.user.mapper.StudentMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学生Service
 *
 * @author tangyi
 * @date 2019/07/09 15:28
 */
@AllArgsConstructor
@Service
public class StudentService extends CrudService<StudentMapper, Student> {

    private final UserService userService;

    private final UserStudentService userStudentService;

    /**
     * 新增学生
     *
     * @param studentDto studentDto
     * @return int
     * @author tangyi
     * @date 2019/07/10 18:18:04
     */
    @Transactional
    public int add(StudentDto studentDto) {
        String currentUser = SysUtil.getUser(), tenantCode = SysUtil.getTenantCode(), userId = studentDto.getUserId();
        if (StringUtils.isBlank(userId)) {
            // 查询当前用户
            UserVo userVo = userService.findUserByIdentifier(currentUser, tenantCode);
            if (userVo == null)
                throw new CommonException("获取当前用户详细信息失败");
            userId = userVo.getId();
        }
        Student student = new Student();
        student.setCommonValue(currentUser, SysUtil.getSysCode(), tenantCode);
        BeanUtils.copyProperties(studentDto, student);
        // 新增用户学生关系
        UserStudent userStudent = new UserStudent();
        userStudent.setCommonValue(currentUser, SysUtil.getSysCode(), tenantCode);
        userStudent.setUserId(userId);
        userStudent.setStudentId(student.getId());
        // TODO
        userStudent.setRelationshipType(0);
        userStudentService.insert(userStudent);
        // 保存学生
        return this.insert(student);
    }
}