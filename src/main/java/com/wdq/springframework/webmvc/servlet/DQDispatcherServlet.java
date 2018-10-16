package com.wdq.springframework.webmvc.servlet;

import com.wdq.springframework.webmvc.anotation.DQAutowired;
import com.wdq.springframework.webmvc.anotation.DQController;
import com.wdq.springframework.webmvc.anotation.DQRequestMapping;
import com.wdq.springframework.webmvc.anotation.DQService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 继承重写HttpServlet
 * 实现SpringMvc
 *
 * @Author: wudq
 * @Date: 2018/10/13
 */
public class DQDispatcherServlet extends HttpServlet {

    //ioc容器
    private Map<String, Object> ioc = new HashMap<String, Object>();
    //handlerMapping容器
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();
    //配置包路径下的相关类的全名
    private List<String> classNames = new ArrayList<String>();
    private Properties contextConfig = new Properties();
    /**
     * @Author wudq
     * @Date 2018/10/14
     * @Description
     * 1、加载配置文件
     * 2、扫描配置包路径下的相关类
     * 3、反射实例化相关类
     * 4、IOC容器中的对象进行属性依赖注入
     * 5、初始化HanddlerMapping
     * @Param
    */
    @Override
    public void init(ServletConfig config) throws ServletException {
        //加载配置
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //扫描类
        doScanner(contextConfig.getProperty("scanPackage"));
        //初始化类
        doInstance();
        //自动注入属性
        doAutowired();
        //初始化HandlerMapping，springMvc内容
        initHandlerMapping();
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(DQController.class)) {
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(DQRequestMapping.class)) {
                baseUrl = clazz.getAnnotation(DQRequestMapping.class).value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(DQRequestMapping.class)) {
                    continue;
                }
                String methodUrl = method.getAnnotation(DQRequestMapping.class).value();
                methodUrl = (baseUrl + "/" +methodUrl).replaceAll("/+", "/");
                handlerMapping.put(methodUrl, method);
                System.out.println("初始化HandlerMapping:"+ methodUrl + "  " +method);
            }
        }

    }

    private void doAutowired() {
        Set<Map.Entry<String, Object>> entrySet = ioc.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(DQAutowired.class)) {
                    continue;
                }
                DQAutowired autowired = field.getAnnotation(DQAutowired.class);
                String beanName = autowired.value();
                if(null == beanName ||beanName.isEmpty() ) {
                    beanName = lowerFirstCase(field.getType().getSimpleName());
                }
                field.setAccessible(true);
                System.out.println("----beanName:"+beanName);
                //注解有value，查找ioc容器中的key(value)指向的实例，并注入给属性
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                //推荐使用类的全路径名，防止key冲突
                //初始化使用了注解的类
                if (clazz.isAnnotationPresent(DQController.class)) {
                    ioc.put(lowerFirstCase(clazz.getSimpleName()), clazz.newInstance());
                } else if (clazz.isAnnotationPresent(DQService.class)) {
                    //默认首字母小写类名
                    //指向接口
                    DQService service = clazz.getAnnotation(DQService.class);
                    String value = service.value();
                    if (null != value && !value.isEmpty()) {
                        ioc.put(value, clazz.newInstance());
                    } else {
                        for (Class<?> c : clazz.getInterfaces()) {
                            ioc.put(lowerFirstCase(c.getSimpleName()), clazz.newInstance());
                            break;
                        }
                    }
                }else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File directory = new File(url.getPath());
        if (!directory.isDirectory()) {

        }
        File[] files = directory.listFiles();
        for (File file : files) {
            //如果是目录，递归
            if (file.isDirectory()) {
                doScanner(scanPackage + "." +file.getName());
            } else {
                if(file.getName().endsWith("class")) {
                    classNames.add(scanPackage + "." + file.getName().replace(".class",""));
                }
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.dispatch(req, resp);
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        if(contextPath.equals(uri)) {
            resp.getWriter().write("welcome to dqspring");
        }
        uri = uri.replace(contextPath, "").replaceAll("/+", "/");
        System.out.println("uri:"+uri);
        if(!handlerMapping.containsKey(uri)) {
            resp.getWriter().write("404");
            return;
        }
        Method method = handlerMapping.get(uri);
        System.out.println(method);
//        req.getParameterMap();
//        method.invoke();
    }

    private static String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return(String.valueOf(chars));
    }

    /**
     * 记录Controller中的RquestMapping和Method的对应关系
     * @Author wudq
     * @Date 2018/10/15
    */
    private class Handler {
        protected Object controller; //方法对应的实例
        protected Method method;     //映射方法
        protected Pattern pattern;
        protected Map<String,Integer> paramIndexMapping; //参数和参数顺序
    }


}