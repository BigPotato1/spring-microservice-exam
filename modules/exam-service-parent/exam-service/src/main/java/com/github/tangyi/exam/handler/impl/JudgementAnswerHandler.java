package com.github.tangyi.exam.handler.impl;

import com.github.tangyi.exam.api.constants.AnswerConstant;
import com.github.tangyi.exam.api.dto.SubjectDto;
import com.github.tangyi.exam.api.module.Answer;
import com.github.tangyi.exam.enums.SubjectTypeEnum;
import com.github.tangyi.exam.handler.AbstractAnswerHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 判断题
 * @author tangyi
 * @date 2019/12/8 22:01
 */
@Slf4j
@AllArgsConstructor
@Component
public class JudgementAnswerHandler extends AbstractAnswerHandler {

	@Override
	public SubjectTypeEnum getSubjectType() {
		return SubjectTypeEnum.JUDGEMENT;
	}

	@Override
	public void judge(Answer answer, SubjectDto subject, List<Double> rightScore) {
		if (subject.getAnswer().getAnswer().equalsIgnoreCase(answer.getAnswer())) {
			rightScore.add(subject.getScore());
			answer.setAnswerType(AnswerConstant.RIGHT);
			answer.setScore(subject.getScore());
			answer.setMarkStatus(AnswerConstant.MARKED);
		} else {
			answer.setAnswerType(AnswerConstant.WRONG);
			answer.setScore(0.0);
			answer.setMarkStatus(AnswerConstant.MARKED);
		}
	}
}
