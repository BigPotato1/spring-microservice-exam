package com.github.tangyi.oss.exceptions;

import com.github.tangyi.common.core.exceptions.CommonException;

/**
 * oss exception
 * @author tangyi
 * @date 2019/12/8 8:41 下午
 */
public class OssException extends CommonException {

	public OssException(String msg) {
		super(msg);
	}

	public OssException(Throwable throwable, String msg) {
		super(throwable, msg);
	}
}
