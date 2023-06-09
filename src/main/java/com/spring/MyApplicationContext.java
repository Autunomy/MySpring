package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author:hty
 * @date:2023-04-02 19:43
 * @email:1156388927@qq.com
 * @description: spring的容器类
 */


public class MyApplicationContext {

    //创建spring容器的时候就需要传入一个Class对象 相当于是一个配置文件
    private Class configClass;

    //单例池 存放所有的单例bean
    private ConcurrentHashMap<String,Object> singletonObjets = new ConcurrentHashMap<>();

    //存储BeanDefinition对象
    private ConcurrentHashMap<String,MyBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    //存储beanPostProcessor对象
    private List<MyBeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public MyApplicationContext(Class configClass) {
        //接收到配置类
        this.configClass = configClass;

        //扫描->BeanDefinition
        scan(configClass);

        //将beanDefinitionMap中所有的单例Bean都创建出来
        for (Map.Entry<String,MyBeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            MyBeanDefinition beanDefinition = entry.getValue();
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName,beanDefinition);//创建
                singletonObjets.put(beanName,bean);
            }
        }

    }

    /***
     * 根据beanDefinition创建Bean对象
     * @param beanDefinition
     * @return
     */
    public Object createBean(String beanName,MyBeanDefinition beanDefinition){
        //创建bean
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.newInstance();

            //依赖注入
            //遍历实例的所有属性字段
            for (Field declaredField : clazz.getDeclaredFields()) {
                //如果字段上面有MyAutowired注解才需要进行注入
                if(declaredField.isAnnotationPresent(MyAutowired.class)){
                    //给属性注入值 这里我们只实现按照属性名称注入
                    Object bean = getBean(declaredField.getName());

                    declaredField.setAccessible(true);//允许修改私有属性
                    declaredField.set(instance,bean);
                }
            }

            //判断类是否实现了MyBeanNameAware接口 Aware回调
            if(instance instanceof MyBeanNameAware){
                //向类中的beanName属性注入值
                ((MyBeanNameAware)instance).setBeanName(beanName);
            }

            //BeanPostProcessor Bean的前置处理器
            for (MyBeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }


            //初始化
            if(instance instanceof MyInitializingBean){
                //调用对应方法
                ((MyInitializingBean)instance).afterPropertiesSet();
            }

            //BeanPostProcessor Bean的后置处理器
            for (MyBeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }


            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 扫描配置类配置的路径下的所有被spring管理的类
     * @param configClass
     */
    private void scan(Class configClass){
        //解析配置类 将MyComponentScan注解 ---> 拿到扫描路径 ---> 扫描
        //判断配置类上有没有MyComponentScan这个注解
        MyComponentScan myComponentScan = (MyComponentScan) configClass.getDeclaredAnnotation(MyComponentScan.class);
        //扫描路径
        String path = myComponentScan.value();
        //扫描包含有MyComponent注解的类
        //要获取包下的类需要使用到类加载器 应用类加载器是加载用户的类的加载器，所以使用应用类加载器获取类
        ClassLoader classLoader = MyApplicationContext.class.getClassLoader();//这个就是应用类加载器
        //通过类加载器获取包路径对应的绝对路径  绝对路径中包含有class文件
        URL resource = classLoader.getResource(path.replace(".","/"));
        //获取绝对路径对应的文件类
        File file = new File(resource.getFile());
        //判断是文件还是目录 注意如果项目路径包含中文，就会判断出错
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                //获取class文件 注意这里穿进去的应该是类路径且后面不带.class后缀  例如com.hty.user.UserService
                Class<?> clazz = null;
                try {
                    clazz = classLoader.loadClass(path + "." + f.getName().replace(".class",""));

                    //判断当前类是否是一个BeanPostProcessor,即判断当前类是否实现了MyBeanPostProcessor接口
                    if(MyBeanPostProcessor.class.isAssignableFrom(clazz)){
                        //得到BeanPostProcessor对象
                        MyBeanPostProcessor beanPostProcessor = (MyBeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                        beanPostProcessorList.add(beanPostProcessor);//存储到list中
                    }

                    //判断类上面是否包含MyComponent注解
                    if(clazz.isAnnotationPresent(MyComponent.class)){
                        //当前类是一个Bean 判断当前Bean的作用域是单例还是多例
                        //由于每次扫描包或者getBean的时候都需要解析字符串，获取类的class对象，所以引出了BeanDefinition对象
                        //BeanDefinition表示Bean的定义 每解析一个类都会生成一个BeanDefinition对象
                        //获取当前Bean的名称
                        MyComponent component = clazz.getDeclaredAnnotation(MyComponent.class);
                        String beanName = component.value();

                        MyBeanDefinition beanDefinition = new MyBeanDefinition();
                        beanDefinition.setClazz(clazz);//将class对象设置进去
                        //解析类
                        if(clazz.isAnnotationPresent(MyScope.class)){//如果存在
                            MyScope myScope = clazz.getDeclaredAnnotation(MyScope.class);
                            beanDefinition.setScope(myScope.value());//设置为用户设置的类型
                        }else{//默认为单例
                            beanDefinition.setScope("singleton");
                        }

                        //放入map中
                        beanDefinitionMap.put(beanName,beanDefinition);

                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * 获取Bean
     * @param beanName bean的名称
     * @return
     */
    public Object getBean(String beanName) throws Exception {
        //判断是否存在当前BeanDefinition
        if(beanDefinitionMap.containsKey(beanName)){
            MyBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){//单例bean
                return singletonObjets.get(beanName);
            }else{//多例
                //创建bean
                return createBean(beanName,beanDefinition);
            }
        }else{
            throw new Exception("bean " + beanName + " 不存在");
        }
    }
}
