package cn.glieen.servlet;

import cn.glieen.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> beans = new HashMap<>();

    private Map<String, Method> requestMappings = new HashMap<>();

    private Map<Method, Object> methodObjectMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) {
        String baseScanPackage = config.getInitParameter("baseScanPackage");
        scanPackage(baseScanPackage);
        createBeans();
        autowired();
        requestAndMethodMapping();
        System.out.println("MVC Is Running.");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        Object result = null;
        String uri = request.getRequestURI();
        Method method = requestMappings.get(uri);
        if (method == null) {
            response.getWriter().write("404 NOT FOUND!");
            return;
        }
        Object object = methodObjectMap.get(method);
        try {
            Parameter[] parameters = method.getParameters();
            Class<?> type = method.getReturnType();
            if (parameters == null || parameters.length == 0) {
                result = type.cast(method.invoke(object));
            } else {
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    if (parameter.isAnnotationPresent(RequestParam.class)) {
                        RequestParam annotation = parameter.getAnnotation(RequestParam.class);
                        String parameterName = annotation.value();
                        String parameterValue = request.getParameter(parameterName);
                        args[i] = parameterValue;
                    } else {
                        args[i] = null;
                    }
                }
                result = type.cast(method.invoke(object, args));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (result == null) {
            response.getWriter().write("null");
        } else {
            response.getWriter().write(result.toString());
        }
    }

    private void scanPackage(String baseScanPackage) {
        URL url = this.getClass().getClassLoader().getResource(baseScanPackage.replaceAll("\\.", "/"));// 将所有的.转义获取对应的路径
        if (url == null) {
            return;
        }
        String fileName = url.getFile();
        File file = new File(fileName);
        String fileList[] = file.list();
        if (fileList == null) {
            return;
        }
        for (String path : fileList) {
            File eachFile = new File(fileName + path);
            if (eachFile.isDirectory()) {
                scanPackage(baseScanPackage + "." + eachFile.getName());
            } else {
                classNames.add(baseScanPackage + "." + eachFile.getName());
            }
        }
    }

    private void createBeans() {
        if (classNames.size() <= 0) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className.substring(0, className.indexOf(".class")));
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Object controller = clazz.newInstance();
                    Controller annotation = clazz.getAnnotation(Controller.class);
                    String clazzName = annotation.value();
                    beans.put(clazzName, controller);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Object service = clazz.newInstance();
                    Service annotation = clazz.getAnnotation(Service.class);
                    String clazzName = annotation.value();
                    beans.put(clazzName, service);
                }
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    private void autowired() {
        Set<Map.Entry<String, Object>> entries = beans.entrySet();
        if (entries.size() <= 0) {
            return;
        }
        for (Map.Entry<String, Object> entry : entries) {
            Object object = entry.getValue();
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        Autowired annotation = field.getAnnotation(Autowired.class);
                        field.setAccessible(true);
                        field.set(object, beans.get(annotation.value()));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void requestAndMethodMapping() {
        Set<Map.Entry<String, Object>> entries = beans.entrySet();
        if (entries.size() <= 0) {
            return;
        }
        for (Map.Entry<String, Object> entry : entries) {
            Object object = entry.getValue();
            Class<?> clazz = object.getClass();
            if (clazz.isAnnotationPresent(Controller.class)) {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                        String requestMapping = annotation.value();
                        requestMappings.put(requestMapping, method);
                        methodObjectMap.put(method, object);
                    }
                }
            }
        }
    }
}
