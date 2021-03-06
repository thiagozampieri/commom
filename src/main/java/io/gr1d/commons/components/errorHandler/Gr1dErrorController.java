package io.gr1d.commons.components.errorHandler;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import io.gr1d.commons.components.errorHandler.response.Gr1dError;

@RestController("${server.error.path:${error.path:/error}}")
public class Gr1dErrorController implements ErrorController {
	private static final Logger LOG = LoggerFactory.getLogger(Gr1dErrorController.class);

	private final String errorPath;
	private final ErrorAttributes errorAttributes;
	private final MessageSource messageSource;

	@Autowired
	public Gr1dErrorController(@Value("${server.error.path:${error.path:/error}}") final String errorPath,
							   final ErrorAttributes errorAttributes, final MessageSource messageSource) {
		this.errorPath = errorPath;
		this.errorAttributes = errorAttributes;
		this.messageSource = messageSource;
	}

	@Override
	public String getErrorPath() {
		return errorPath;
	}

	@RequestMapping(path = "/error")
	public ResponseEntity<Collection<Gr1dError>> error(final HttpServletRequest request, final HttpServletResponse response, final Locale locale) {
		final Collection<Gr1dError> errors = new ArrayList<>(1);
		errors.add(new Gr1dError(getErrorAttributes(request)));

		errors.forEach(err -> err.translateMessage(messageSource, locale));

		final Map<String, Object> errorAttribs = getErrorAttributes(request);
		final int statusCode = (int) errorAttribs.get("status");

		return ResponseEntity.status(statusCode == 999 ? NOT_FOUND.value() : statusCode).body(errors);
	}

	private Map<String, Object> getErrorAttributes(final HttpServletRequest request) {
		return errorAttributes.getErrorAttributes(new ServletWebRequest(request), LOG.isDebugEnabled());
	}
}
