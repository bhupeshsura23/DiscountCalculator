/**
 * 
 */
package com.bsura.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 */
@ResponseStatus(code = HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = -3985937915718625241L;

}
