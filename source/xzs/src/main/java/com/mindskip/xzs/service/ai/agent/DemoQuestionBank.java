package com.mindskip.xzs.service.ai.agent;

import java.util.*;

/**
 * 内置兜底题库：当 LLM 不可用时，根据 ExamRequirement 返回结构化题目，
 * 保证 Demo 演示流程不中断。题目覆盖 Java / Python / MySQL 等常见科目。
 */
final class DemoQuestionBank {

    private DemoQuestionBank() {}

    private static final List<Map<String, Object>> JAVA_POOL = buildPool(
            q(1, "Java 中用于声明类继承关系的关键字是？", new String[]{"implements", "extends", "super", "import"}, "B", "类继承使用 extends；实现接口使用 implements。"),
            q(1, "下列数据类型中不属于 Java 基本数据类型的是？", new String[]{"int", "float", "String", "boolean"}, "C", "String 是引用类型，其余均为基本数据类型。"),
            q(1, "Java 应用程序的入口方法声明正确的是？", new String[]{"public void main(String[] args)", "public static int main(String[] args)", "public static void main(String[] args)", "static private main()"}, "C", "入口方法必须是 public static void 且参数为 String 数组。"),
            q(1, "执行 String s = \"abc\"; s.concat(\"def\"); 后 s 的值为？", new String[]{"abcdef", "abc", "def", "报错"}, "B", "String 不可变，concat 返回新字符串，原对象不变。"),
            q(1, "ArrayList 与 LinkedList 的主要区别是？", new String[]{"ArrayList 底层是链表", "ArrayList 底层是动态数组，LinkedList 是双向链表", "两者完全相同", "LinkedList 不允许重复"}, "B", "ArrayList 基于动态数组随机访问快；LinkedList 基于双向链表增删快。"),
            q(2, "下列属于 Java 面向对象三大特性的有？", new String[]{"封装", "继承", "多态", "编译"}, "A,B,C", "封装、继承、多态是 OOP 三大特性。"),
            q(2, "下列属于 Spring Boot 主要优点的有？", new String[]{"自动配置减少 XML", "内嵌 Web 容器开箱即用", "starter 起步依赖简化构建", "无需编写任何代码"}, "A,B,C", "自动配置、内嵌容器、starter 是核心优势。"),
            q(3, "Java 中 int 类型在 32 位和 64 位平台都占 4 字节。", new String[]{"正确", "错误"}, "A", "int 字节宽度由 JVM 规范统一规定为 4 字节。"),
            q(3, "@RestController 等价于 @Controller 与 @ResponseBody 组合。", new String[]{"正确", "错误"}, "A", "@RestController 是组合注解，返回值直接写入响应体。"),
            q(1, "Spring Boot 启动类最核心的组合注解是？", new String[]{"@SpringBootApplication", "@Controller", "@Component", "@Repository"}, "A", "@SpringBootApplication 组合了核心注解。"),
            q(1, "Spring 中最常用于自动装配 Bean 的注解是？", new String[]{"@Autowired", "@Override", "@Deprecated", "@SuppressWarnings"}, "A", "@Autowired 默认按类型注入。"),
            q(2, "Spring 常见的依赖注入方式包括？", new String[]{"构造方法注入", "Setter 注入", "字段注入 @Autowired", "new 直接创建"}, "A,B,C", "Spring 支持构造器、Setter、字段注入。"),
            q(1, "在 application.yml 中修改服务端口应配置？", new String[]{"server.port", "spring.port", "service.port", "web.port"}, "A", "通过 server.port 指定内嵌容器端口。"),
            q(3, "ArrayList 是线程安全的集合类。", new String[]{"正确", "错误"}, "B", "ArrayList 非线程安全，多线程环境需用 CopyOnWriteArrayList 或同步包装。")
    );

    private static final List<Map<String, Object>> PYTHON_POOL = buildPool(
            q(1, "Python 中定义函数使用的关键字是？", new String[]{"func", "def", "function", "define"}, "B", "Python 使用 def 定义函数。"),
            q(1, "下列哪个不是 Python 的基本数据类型？", new String[]{"int", "str", "char", "bool"}, "C", "Python 没有 char 类型，单字符也是 str。"),
            q(1, "Python 中列表 list 与元组 tuple 的主要区别是？", new String[]{"list 有序 tuple 无序", "list 可变 tuple 不可变", "list 只能存数字", "无区别"}, "B", "list 可变，tuple 不可变。"),
            q(2, "下列属于 Python 内置数据结构的有？", new String[]{"list", "dict", "set", "array"}, "A,B,C", "list、dict、set 是内置结构，array 需导入模块。"),
            q(3, "Python 中字典的键可以是任意类型。", new String[]{"正确", "错误"}, "B", "字典的键必须是可哈希的不可变类型。"),
            q(1, "Python 中用于异常捕获的关键字组合是？", new String[]{"try-catch", "try-except", "catch-finally", "throw-try"}, "B", "Python 使用 try-except 捕获异常。"),
            q(1, "下列哪个方法用于向列表末尾添加元素？", new String[]{"append()", "add()", "insert()", "push()"}, "A", "append() 在列表末尾添加元素。"),
            q(3, "Python 是解释型语言，不需要编译即可运行。", new String[]{"正确", "错误"}, "A", "Python 是解释型语言，由解释器逐行执行。"),
            q(2, "下列关于 Python 装饰器说法正确的有？", new String[]{"装饰器本质是函数", "可以在不修改原函数的情况下扩展功能", "使用 @ 语法糖调用", "只能装饰类"}, "A,B,C", "装饰器是接收函数返回函数的高阶函数。"),
            q(1, "Python 中实现多继承时，方法解析顺序使用什么算法？", new String[]{"BFS", "DFS", "C3 线性化", "随机"}, "C", "Python 使用 C3 线性化算法确定 MRO。"),
            q(3, "Python 中 == 与 is 的作用完全相同。", new String[]{"正确", "错误"}, "B", "== 比较值，is 比较对象身份（内存地址）。"),
            q(1, "下列哪个库常用于 Python 数据分析？", new String[]{"requests", "pandas", "flask", "django"}, "B", "pandas 是数据分析核心库。")
    );

    private static final List<Map<String, Object>> MYSQL_POOL = buildPool(
            q(1, "下列 SQL 关键字用于查询数据的是？", new String[]{"INSERT", "UPDATE", "SELECT", "DELETE"}, "C", "SELECT 用于查询数据。"),
            q(1, "MySQL 中适合存储可变长度字符串的类型是？", new String[]{"CHAR", "VARCHAR", "INT", "DATE"}, "B", "VARCHAR 按实际长度存储。"),
            q(1, "查询年龄大于 18 的学生，WHERE 子句正确的是？", new String[]{"WHERE age > 18", "WHERE age equals 18", "WHERE age == 18", "WHEN age > 18"}, "A", "SQL 使用 > 进行大于比较。"),
            q(2, "下列属于 SQL 聚合函数的有？", new String[]{"COUNT", "SUM", "AVG", "MAX"}, "A,B,C,D", "COUNT/SUM/AVG/MAX 都是聚合函数。"),
            q(3, "MySQL 的 InnoDB 存储引擎支持事务和外键约束。", new String[]{"正确", "错误"}, "A", "InnoDB 支持 ACID 事务、行级锁和外键。"),
            q(1, "用于去除查询结果重复行的关键字是？", new String[]{"UNIQUE", "DISTINCT", "DIFFERENT", "ONLY"}, "B", "DISTINCT 去除重复行。"),
            q(1, "下列哪个不是 MySQL 的数据类型？", new String[]{"INT", "VARCHAR", "BOOLEAN", "STRING"}, "D", "MySQL 没有 STRING 类型，对应 VARCHAR/TEXT。"),
            q(3, "DELETE 语句可以带 WHERE 条件删除指定行。", new String[]{"正确", "错误"}, "A", "DELETE 配合 WHERE 可删除指定行，不加则删除全部。"),
            q(2, "下列关于索引说法正确的有？", new String[]{"索引可加速查询", "索引会占用额外存储空间", "索引会降低插入更新速度", "索引越多越好"}, "A,B,C", "索引加速查询但占空间且降低写性能。"),
            q(1, "连接两张表时使用的关键字是？", new String[]{"LINK", "JOIN", "CONNECT", "MERGE"}, "B", "使用 JOIN 进行表连接。"),
            q(3, "TRUNCATE 与 DELETE 删除数据效果完全相同。", new String[]{"正确", "错误"}, "B", "TRUNCATE 是 DDL 不可回滚且重置自增，DELETE 是 DML 可回滚。"),
            q(1, "统计记录数应使用哪个聚合函数？", new String[]{"SUM", "AVG", "COUNT", "TOTAL"}, "C", "COUNT(*) 统计记录数。")
    );

    private static final Map<String, List<Map<String, Object>>> POOLS = new HashMap<>();
    static {
        POOLS.put("java", JAVA_POOL);
        POOLS.put("python", PYTHON_POOL);
        POOLS.put("mysql", MYSQL_POOL);
    }

    /**
     * 根据需求从对应科目题库中抽取题目，尽量覆盖请求的题型。
     */
    static List<Map<String, Object>> generate(ExamRequirement req) {
        String subject = req.getSubject() != null ? req.getSubject().toLowerCase() : "java";
        List<Map<String, Object>> pool = POOLS.getOrDefault(subject, JAVA_POOL);
        int count = req.getQuestionCount() != null ? req.getQuestionCount() : 10;
        List<Integer> types = req.getQuestionTypes() != null && !req.getQuestionTypes().isEmpty()
                ? req.getQuestionTypes() : Collections.singletonList(1);

        List<Map<String, Object>> result = new ArrayList<>();
        // 先按题型优先抽取
        for (int type : types) {
            for (Map<String, Object> q : pool) {
                if (result.size() >= count) break;
                if ((int) q.get("questionType") == type && !result.contains(q)) {
                    result.add(copy(q));
                }
            }
        }
        // 不足则用其他题型补足
        for (Map<String, Object> q : pool) {
            if (result.size() >= count) break;
            if (!result.contains(q)) result.add(copy(q));
        }
        return result;
    }

    private static Map<String, Object> q(int type, String question, String[] options, String answer, String analysis) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("questionType", type);
        m.put("question", question);
        m.put("options", new ArrayList<>(Arrays.asList(options)));
        m.put("correct_answer", answer);
        m.put("analysis", analysis);
        m.put("knowledge_point", "");
        m.put("difficulty", 2);
        m.put("qualityScore", 4.8);
        return m;
    }

    private static List<Map<String, Object>> buildPool(Map<String, Object>... qs) {
        return new ArrayList<>(Arrays.asList(qs));
    }

    private static Map<String, Object> copy(Map<String, Object> src) {
        return new LinkedHashMap<>(src);
    }
}
