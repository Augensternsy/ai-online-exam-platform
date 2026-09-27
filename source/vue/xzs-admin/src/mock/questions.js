/**
 * Mock 题目数据
 */

export const mockQuestions = [
  {
    id: 1,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 1,
    score: 1000,
    infoTextContentId: 1,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Spring Boot 项目的启动类通常使用以下哪个注解？',
    options: ['@SpringBootApplication', '@Configuration', '@ComponentScan', '@EnableScheduling'],
    correctAnswer: 'A',
    analysis: '@SpringBootApplication 是核心组合注解，包含 @SpringBootConfiguration、@EnableAutoConfiguration 和 @ComponentScan。',
    knowledgePoint: 'Spring Boot 核心'
  },
  {
    id: 2,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 1,
    score: 1000,
    infoTextContentId: 2,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '在未手动替换的情况下，Spring Boot 默认内嵌的 Web 容器是？',
    options: ['Jetty', 'Tomcat', 'Undertow', 'Netty'],
    correctAnswer: 'B',
    analysis: 'spring-boot-starter-web 默认引入 Tomcat；如需 Jetty 或 Undertow，需要排除 Tomcat 后手动引入。',
    knowledgePoint: 'Spring Boot 核心'
  },
  {
    id: 3,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 1,
    score: 1000,
    infoTextContentId: 3,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '在 application.yml 中要将服务端口修改为 8081，正确的写法是？',
    options: ['server.port: 8081', 'spring.port: 8081', 'http.port: 8081', 'port: 8081'],
    correctAnswer: 'A',
    analysis: '服务端口通过 server.port 属性配置，该属性作用于内嵌 Web 容器。',
    knowledgePoint: '配置文件'
  },
  {
    id: 4,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 1,
    score: 1000,
    infoTextContentId: 4,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '下列注解中，通常用于标注业务逻辑层（Service 层）组件的是？',
    options: ['@Service', '@Controller', '@RestController', '@Configuration'],
    correctAnswer: 'A',
    analysis: '@Service 用于标注业务逻辑层组件，会被 Spring 容器扫描并注册为 Bean。',
    knowledgePoint: '分层注解'
  },
  {
    id: 5,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 5,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '关于 Java 中 == 运算符和 equals() 方法的区别，以下说法正确的是？',
    options: [
      '对于基本数据类型，== 比较值；对于引用类型，== 比较引用地址',
      'equals() 方法可以比较基本数据类型的值',
      'String 类的 equals() 方法比较的是字符串的内容',
      '所有类的 equals() 方法默认都是比较引用地址'
    ],
    correctAnswer: 'A',
    analysis: '== 对基本数据类型比较值，对引用类型比较是否指向同一对象。String 重写了 equals() 方法比较内容。',
    knowledgePoint: 'Java 基础'
  },
  {
    id: 6,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 6,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Java 中，以下哪个关键字用于定义常量？',
    options: ['const', 'final', 'static', 'abstract'],
    correctAnswer: 'B',
    analysis: 'final 关键字用于定义常量，表示该变量赋值后不可修改。Java 没有 const 关键字。',
    knowledgePoint: 'Java 基础'
  },
  {
    id: 7,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 7,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '下列关于 Java 中 ArrayList 和 LinkedList 的说法，正确的是？',
    options: [
      'ArrayList 基于链表实现，LinkedList 基于数组实现',
      'ArrayList 随机访问效率高，LinkedList 插入删除效率高',
      'ArrayList 和 LinkedList 都支持高效的随机访问',
      'ArrayList 是线程安全的，LinkedList 不是'
    ],
    correctAnswer: 'B',
    analysis: 'ArrayList 基于动态数组实现，随机访问 O(1)；LinkedList 基于双向链表，插入删除 O(1)（在头尾操作时）。',
    knowledgePoint: 'Java 集合'
  },
  {
    id: 8,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 8,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Java 中，以下哪个方法用于启动一个线程？',
    options: ['run()', 'start()', 'execute()', 'begin()'],
    correctAnswer: 'B',
    analysis: 'start() 方法会创建新线程并调用 run() 方法；直接调用 run() 不会创建新线程，只是普通方法调用。',
    knowledgePoint: 'Java 多线程'
  },
  {
    id: 9,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 9,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '关于 Java 中的接口（interface），以下说法错误的是？',
    options: [
      '接口中的方法默认是 public abstract 的',
      '接口中的变量默认是 public static final 的',
      '一个类可以实现多个接口',
      '接口可以有构造方法'
    ],
    correctAnswer: 'D',
    analysis: '接口不能有构造方法，因为接口不能被实例化。接口中的方法默认 public abstract，变量默认 public static final。',
    knowledgePoint: 'Java 面向对象'
  },
  {
    id: 10,
    questionType: 1,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 10,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Java 中，以下哪个类是所有类的父类？',
    options: ['Class', 'Object', 'Super', 'Base'],
    correctAnswer: 'B',
    analysis: 'Object 类是 Java 中所有类的直接或间接父类，所有类都继承自 Object。',
    knowledgePoint: 'Java 基础'
  },
  {
    id: 11,
    questionType: 2,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 11,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '下列属于 Spring Boot 主要优点的有？',
    options: ['自动配置，减少繁琐的 XML 配置', '内嵌 Web 容器，开箱即用', '提供 starter 起步依赖，简化构建', '只能在 Linux 操作系统上运行'],
    correctAnswer: 'A,B,C',
    analysis: '自动配置、内嵌容器、starter 依赖是 Spring Boot 的核心优势；它不限制操作系统。',
    knowledgePoint: 'Spring Boot 特性'
  },
  {
    id: 12,
    questionType: 2,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 12,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Spring 中常见的依赖注入方式包括下列哪些？',
    options: ['构造方法注入', 'Setter 方法注入', '字段注入（@Autowired）', '在类中使用 new 直接创建对象'],
    correctAnswer: 'A,B,C',
    analysis: 'Spring 支持构造器注入、Setter 注入和字段注入；new 创建的对象不受 Spring 容器管理，不属于依赖注入。',
    knowledgePoint: '依赖注入'
  },
  {
    id: 13,
    questionType: 2,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 13,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '下列关于 Java 集合框架的说法，正确的有？',
    options: [
      'HashMap 允许 key 和 value 为 null',
      'TreeMap 会对 key 进行排序',
      'HashSet 中的元素是有序的',
      'ArrayList 是线程安全的'
    ],
    correctAnswer: 'A,B',
    analysis: 'HashMap 允许 null key 和 null value；TreeMap 基于红黑树，按 key 排序；HashSet 无序；ArrayList 非线程安全。',
    knowledgePoint: 'Java 集合'
  },
  {
    id: 14,
    questionType: 2,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 14,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '下列关于 Java 异常处理的说法，正确的有？',
    options: [
      'try 块可以单独存在，不需要 catch 或 finally',
      'finally 块中的代码一定会执行',
      '一个 try 可以对应多个 catch',
      'throw 用于抛出异常，throws 用于声明异常'
    ],
    correctAnswer: 'B,C,D',
    analysis: 'try 必须与 catch 或 finally 一起使用；finally 无论是否异常都会执行（除 System.exit）；一个 try 可对应多个 catch；throw 抛出异常，throws 声明异常。',
    knowledgePoint: 'Java 异常'
  },
  {
    id: 15,
    questionType: 3,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 1,
    score: 1000,
    infoTextContentId: 15,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '在控制器类上使用 @RestController，其效果等价于同时使用 @Controller 和 @ResponseBody。',
    options: ['正确', '错误'],
    correctAnswer: 'A',
    analysis: '@RestController 是 @Controller 与 @ResponseBody 的组合注解，方法返回值默认直接序列化为 JSON 写入响应体。',
    knowledgePoint: 'Web 注解'
  },
  {
    id: 16,
    questionType: 3,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 16,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: '@Autowired 注解只能按照 Bean 的名称进行注入，无法按类型注入。',
    options: ['正确', '错误'],
    correctAnswer: 'B',
    analysis: '@Autowired 默认按类型（byType）注入，配合 @Qualifier 注解时可以按名称指定具体的 Bean。',
    knowledgePoint: '依赖注入'
  },
  {
    id: 17,
    questionType: 3,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 1,
    score: 1000,
    infoTextContentId: 17,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Java 中的 String 类是不可变的（immutable）。',
    options: ['正确', '错误'],
    correctAnswer: 'A',
    analysis: 'String 类被声明为 final，且内部字符数组也是 final，因此 String 对象一旦创建就不能被修改。',
    knowledgePoint: 'Java 基础'
  },
  {
    id: 18,
    questionType: 3,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 18,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Java 中的 static 方法可以直接访问非 static 成员变量。',
    options: ['正确', '错误'],
    correctAnswer: 'B',
    analysis: 'static 方法属于类，而非 static 成员属于实例，static 方法中不能直接访问非 static 成员，必须通过对象实例访问。',
    knowledgePoint: 'Java 基础'
  },
  {
    id: 19,
    questionType: 3,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 19,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Java 中的抽象类（abstract class）不能被实例化。',
    options: ['正确', '错误'],
    correctAnswer: 'A',
    analysis: '抽象类包含抽象方法，不能直接实例化，必须通过子类继承并实现抽象方法后才能创建对象。',
    knowledgePoint: 'Java 面向对象'
  },
  {
    id: 20,
    questionType: 3,
    subjectId: 1,
    gradeLevel: 1,
    difficult: 2,
    score: 1000,
    infoTextContentId: 20,
    createUser: 1,
    status: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    question: 'Java 中的 final 关键字可以用于修饰类、方法和变量。',
    options: ['正确', '错误'],
    correctAnswer: 'A',
    analysis: 'final 可以修饰类（不可继承）、方法（不可重写）和变量（常量），是 Java 中非常常用的修饰符。',
    knowledgePoint: 'Java 基础'
  }
]

export const mockQuestionPageList = (query) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const pageIndex = query?.pageIndex || 1
      const pageSize = query?.pageSize || 10
      const questionType = query?.questionType
      const subjectId = query?.subjectId
      const question = query?.question || ''

      let filtered = mockQuestions.filter(q => {
        if (questionType && q.questionType !== questionType) return false
        if (subjectId && q.subjectId !== subjectId) return false
        if (question && !q.question.includes(question)) return false
        return true
      })

      resolve({
        code: 1,
        response: {
          list: filtered.slice((pageIndex - 1) * pageSize, pageIndex * pageSize),
          total: filtered.length,
          pageIndex,
          pageSize
        }
      })
    }, 300)
  })
}

export const mockSelectQuestion = (id) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const q = mockQuestions.find(item => item.id === id)
      resolve({
        code: 1,
        response: q || null
      })
    }, 300)
  })
}
