package com.example.backend.config;

import com.example.backend.common.BusinessException;
import com.example.backend.common.CurrentUser;
import com.example.backend.common.LoginUser;
import com.example.backend.common.ResultCode;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 将请求属性中的登录用户注入到标注 @CurrentUser 的 Controller 方法参数。
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && LoginUser.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        boolean required = annotation == null || annotation.required();
        LoginUser user = (LoginUser) webRequest.getAttribute(
                AuthInterceptor.ATTR_USER, RequestAttributes.SCOPE_REQUEST);
        if (user == null && required) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }
}
