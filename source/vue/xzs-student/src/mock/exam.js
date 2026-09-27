/**
 * Mock 试卷和题目数据（学生端）
 * Java 基础能力测试 - 20 道题
 *
 * 数据格式与后端 /api/student/exam/paper/select/{id} 返回结构一致：
 * - titleItems: 试卷大题分组（单选/多选/判断）
 * - 每个 questionItem: { id, itemOrder, questionType, title, items, score, analyze, correct, difficult }
 * - items: [{ prefix: 'A', content: '...' }]
 */

// ---------- 单选题（1-10） ----------
const singleChoiceQuestions = [
  {
    id: 1,
    itemOrder: 1,
    questionType: 1,
    title: 'Java 中，以下哪个关键字用于定义常量？',
    items: [
      { prefix: 'A', content: 'const' },
      { prefix: 'B', content: 'final' },
      { prefix: 'C', content: 'static' },
      { prefix: 'D', content: 'abstract' }
    ],
    correct: 'B',
    analyze: 'final 关键字用于定义常量，表示该变量赋值后不可修改。Java 没有 const 关键字。',
    score: '5',
    difficult: 2
  },
  {
    id: 2,
    itemOrder: 2,
    questionType: 1,
    title: '关于 Java 中 == 运算符和 equals() 方法的区别，以下说法正确的是？',
    items: [
      { prefix: 'A', content: '对于基本数据类型，== 比较值；对于引用类型，== 比较引用地址' },
      { prefix: 'B', content: 'equals() 方法可以比较基本数据类型的值' },
      { prefix: 'C', content: 'String 类的 equals() 方法比较的是字符串的内容' },
      { prefix: 'D', content: '所有类的 equals() 方法默认都是比较引用地址' }
    ],
    correct: 'A',
    analyze: '== 对基本数据类型比较值，对引用类型比较是否指向同一对象。String 重写了 equals() 方法比较内容。',
    score: '5',
    difficult: 3
  },
  {
    id: 3,
    itemOrder: 3,
    questionType: 1,
    title: '下列关于 Java 中 ArrayList 和 LinkedList 的说法，正确的是？',
    items: [
      { prefix: 'A', content: 'ArrayList 基于链表实现，LinkedList 基于数组实现' },
      { prefix: 'B', content: 'ArrayList 随机访问效率高，LinkedList 插入删除效率高' },
      { prefix: 'C', content: 'ArrayList 和 LinkedList 都支持高效的随机访问' },
      { prefix: 'D', content: 'ArrayList 是线程安全的，LinkedList 不是' }
    ],
    correct: 'B',
    analyze: 'ArrayList 基于动态数组实现，随机访问 O(1)；LinkedList 基于双向链表，插入删除 O(1)（在头尾操作时）。',
    score: '5',
    difficult: 3
  },
  {
    id: 4,
    itemOrder: 4,
    questionType: 1,
    title: 'Java 中，以下哪个方法用于启动一个线程？',
    items: [
      { prefix: 'A', content: 'run()' },
      { prefix: 'B', content: 'start()' },
      { prefix: 'C', content: 'execute()' },
      { prefix: 'D', content: 'begin()' }
    ],
    correct: 'B',
    analyze: 'start() 方法会创建新线程并调用 run() 方法；直接调用 run() 不会创建新线程，只是普通方法调用。',
    score: '5',
    difficult: 2
  },
  {
    id: 5,
    itemOrder: 5,
    questionType: 1,
    title: '关于 Java 中的接口（interface），以下说法错误的是？',
    items: [
      { prefix: 'A', content: '接口中的方法默认是 public abstract 的' },
      { prefix: 'B', content: '接口中的变量默认是 public static final 的' },
      { prefix: 'C', content: '一个类可以实现多个接口' },
      { prefix: 'D', content: '接口可以有构造方法' }
    ],
    correct: 'D',
    analyze: '接口不能有构造方法，因为接口不能被实例化。接口中的方法默认 public abstract，变量默认 public static final。',
    score: '5',
    difficult: 2
  },
  {
    id: 6,
    itemOrder: 6,
    questionType: 1,
    title: 'Java 中，以下哪个类是所有类的父类？',
    items: [
      { prefix: 'A', content: 'Class' },
      { prefix: 'B', content: 'Object' },
      { prefix: 'C', content: 'Super' },
      { prefix: 'D', content: 'Base' }
    ],
    correct: 'B',
    analyze: 'Object 类是 Java 中所有类的直接或间接父类，所有类都继承自 Object。',
    score: '5',
    difficult: 1
  },
  {
    id: 7,
    itemOrder: 7,
    questionType: 1,
    title: 'Spring Boot 项目的启动类通常使用以下哪个注解？',
    items: [
      { prefix: 'A', content: '@SpringBootApplication' },
      { prefix: 'B', content: '@Configuration' },
      { prefix: 'C', content: '@ComponentScan' },
      { prefix: 'D', content: '@EnableScheduling' }
    ],
    correct: 'A',
    analyze: '@SpringBootApplication 是核心组合注解，包含 @SpringBootConfiguration、@EnableAutoConfiguration 和 @ComponentScan。',
    score: '5',
    difficult: 2
  },
  {
    id: 8,
    itemOrder: 8,
    questionType: 1,
    title: '在未手动替换的情况下，Spring Boot 默认内嵌的 Web 容器是？',
    items: [
      { prefix: 'A', content: 'Jetty' },
      { prefix: 'B', content: 'Tomcat' },
      { prefix: 'C', content: 'Undertow' },
      { prefix: 'D', content: 'Netty' }
    ],
    correct: 'B',
    analyze: 'spring-boot-starter-web 默认引入 Tomcat；如需 Jetty 或 Undertow，需要排除 Tomcat 后手动引入。',
    score: '5',
    difficult: 1
  },
  {
    id: 9,
    itemOrder: 9,
    questionType: 1,
    title: '在 application.yml 中要将服务端口修改为 8081，正确的写法是？',
    items: [
      { prefix: 'A', content: 'server.port: 8081' },
      { prefix: 'B', content: 'spring.port: 8081' },
      { prefix: 'C', content: 'http.port: 8081' },
      { prefix: 'D', content: 'port: 8081' }
    ],
    correct: 'A',
    analyze: '服务端口通过 server.port 属性配置，该属性作用于内嵌 Web 容器。',
    score: '5',
    difficult: 1
  },
  {
    id: 10,
    itemOrder: 10,
    questionType: 1,
    title: '下列注解中，通常用于标注业务逻辑层（Service 层）组件的是？',
    items: [
      { prefix: 'A', content: '@Service' },
      { prefix: 'B', content: '@Controller' },
      { prefix: 'C', content: '@RestController' },
      { prefix: 'D', content: '@Configuration' }
    ],
    correct: 'A',
    analyze: '@Service 用于标注业务逻辑层组件，会被 Spring 容器扫描并注册为 Bean。',
    score: '5',
    difficult: 1
  }
]

// ---------- 多选题（11-14） ----------
const multiChoiceQuestions = [
  {
    id: 11,
    itemOrder: 11,
    questionType: 2,
    title: '下列属于 Spring Boot 主要优点的有？',
    items: [
      { prefix: 'A', content: '自动配置，减少繁琐的 XML 配置' },
      { prefix: 'B', content: '内嵌 Web 容器，开箱即用' },
      { prefix: 'C', content: '提供 starter 起步依赖，简化构建' },
      { prefix: 'D', content: '只能在 Linux 操作系统上运行' }
    ],
    correct: 'A,B,C',
    correctArray: ['A', 'B', 'C'],
    analyze: '自动配置、内嵌容器、starter 依赖是 Spring Boot 的核心优势；它不限制操作系统。',
    score: '5',
    difficult: 2
  },
  {
    id: 12,
    itemOrder: 12,
    questionType: 2,
    title: 'Spring 中常见的依赖注入方式包括下列哪些？',
    items: [
      { prefix: 'A', content: '构造方法注入' },
      { prefix: 'B', content: 'Setter 方法注入' },
      { prefix: 'C', content: '字段注入（@Autowired）' },
      { prefix: 'D', content: '在类中使用 new 直接创建对象' }
    ],
    correct: 'A,B,C',
    correctArray: ['A', 'B', 'C'],
    analyze: 'Spring 支持构造器注入、Setter 注入和字段注入；new 创建的对象不受 Spring 容器管理，不属于依赖注入。',
    score: '5',
    difficult: 3
  },
  {
    id: 13,
    itemOrder: 13,
    questionType: 2,
    title: '下列关于 Java 集合框架的说法，正确的有？',
    items: [
      { prefix: 'A', content: 'HashMap 允许 key 和 value 为 null' },
      { prefix: 'B', content: 'TreeMap 会对 key 进行排序' },
      { prefix: 'C', content: 'HashSet 中的元素是有序的' },
      { prefix: 'D', content: 'ArrayList 是线程安全的' }
    ],
    correct: 'A,B',
    correctArray: ['A', 'B'],
    analyze: 'HashMap 允许 null key 和 null value；TreeMap 基于红黑树，按 key 排序；HashSet 无序；ArrayList 非线程安全。',
    score: '5',
    difficult: 3
  },
  {
    id: 14,
    itemOrder: 14,
    questionType: 2,
    title: '下列关于 Java 异常处理的说法，正确的有？',
    items: [
      { prefix: 'A', content: 'try 块可以单独存在，不需要 catch 或 finally' },
      { prefix: 'B', content: 'finally 块中的代码一定会执行' },
      { prefix: 'C', content: '一个 try 可以对应多个 catch' },
      { prefix: 'D', content: 'throw 用于抛出异常，throws 用于声明异常' }
    ],
    correct: 'B,C,D',
    correctArray: ['B', 'C', 'D'],
    analyze: 'try 必须与 catch 或 finally 一起使用；finally 无论是否异常都会执行（除 System.exit）；一个 try 可对应多个 catch；throw 抛出异常，throws 声明异常。',
    score: '5',
    difficult: 3
  }
]

// ---------- 判断题（15-20） ----------
const trueFalseQuestions = [
  {
    id: 15,
    itemOrder: 15,
    questionType: 3,
    title: '在控制器类上使用 @RestController，其效果等价于同时使用 @Controller 和 @ResponseBody。',
    items: [
      { prefix: 'A', content: '正确' },
      { prefix: 'B', content: '错误' }
    ],
    correct: 'A',
    analyze: '@RestController 是 @Controller 与 @ResponseBody 的组合注解，方法返回值默认直接序列化为 JSON 写入响应体。',
    score: '5',
    difficult: 1
  },
  {
    id: 16,
    itemOrder: 16,
    questionType: 3,
    title: '@Autowired 注解只能按照 Bean 的名称进行注入，无法按类型注入。',
    items: [
      { prefix: 'A', content: '正确' },
      { prefix: 'B', content: '错误' }
    ],
    correct: 'B',
    analyze: '@Autowired 默认按类型（byType）注入，配合 @Qualifier 注解时可以按名称指定具体的 Bean。',
    score: '5',
    difficult: 2
  },
  {
    id: 17,
    itemOrder: 17,
    questionType: 3,
    title: 'Java 中的 String 类是不可变的（immutable）。',
    items: [
      { prefix: 'A', content: '正确' },
      { prefix: 'B', content: '错误' }
    ],
    correct: 'A',
    analyze: 'String 类被声明为 final，且内部字符数组也是 final，因此 String 对象一旦创建就不能被修改。',
    score: '5',
    difficult: 1
  },
  {
    id: 18,
    itemOrder: 18,
    questionType: 3,
    title: 'Java 中的 static 方法可以直接访问非 static 成员变量。',
    items: [
      { prefix: 'A', content: '正确' },
      { prefix: 'B', content: '错误' }
    ],
    correct: 'B',
    analyze: 'static 方法属于类，而非 static 成员属于实例，static 方法中不能直接访问非 static 成员，必须通过对象实例访问。',
    score: '5',
    difficult: 2
  },
  {
    id: 19,
    itemOrder: 19,
    questionType: 3,
    title: 'Java 中的抽象类（abstract class）不能被实例化。',
    items: [
      { prefix: 'A', content: '正确' },
      { prefix: 'B', content: '错误' }
    ],
    correct: 'A',
    analyze: '抽象类包含抽象方法，不能直接实例化，必须通过子类继承并实现抽象方法后才能创建对象。',
    score: '5',
    difficult: 1
  },
  {
    id: 20,
    itemOrder: 20,
    questionType: 3,
    title: 'Java 中的 final 关键字可以用于修饰类、方法和变量。',
    items: [
      { prefix: 'A', content: '正确' },
      { prefix: 'B', content: '错误' }
    ],
    correct: 'A',
    analyze: 'final 可以修饰类（不可继承）、方法（不可重写）和变量（常量），是 Java 中非常常用的修饰符。',
    score: '5',
    difficult: 1
  }
]

export const mockExamQuestions = [
  ...singleChoiceQuestions,
  ...multiChoiceQuestions,
  ...trueFalseQuestions
]

/**
 * 组装试卷详细信息（含 titleItems 分组）
 */
const buildPaperDetail = (paper) => {
  return {
    ...paper,
    suggestTime: 30,
    limitStartTime: null,
    limitEndTime: null,
    titleItems: [
      { name: '单选题', questionItems: singleChoiceQuestions },
      { name: '多选题', questionItems: multiChoiceQuestions },
      { name: '判断题', questionItems: trueFalseQuestions }
    ]
  }
}

export const mockExamPaper = {
  id: 1,
  name: 'Java 基础能力测试',
  paperType: 1,
  subjectId: 1,
  subjectName: 'Java 开发技术',
  gradeLevel: 1,
  score: '100',
  questionCount: 20,
  difficult: 2,
  limitStartTime: null,
  limitEndTime: null,
  frameTextContentId: 1
}

export const mockExamPaperList = [
  mockExamPaper,
  {
    id: 2,
    name: 'Spring Boot 入门测试',
    paperType: 1,
    subjectId: 1,
    subjectName: 'Java 开发技术',
    gradeLevel: 1,
    score: '100',
    questionCount: 8,
    difficult: 1,
    limitStartTime: null,
    limitEndTime: null,
    frameTextContentId: 2
  }
]

export const mockSelectExamPaper = (id) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const paper = mockExamPaperList.find(p => p.id === Number(id))
      if (paper) {
        resolve({
          code: 1,
          response: buildPaperDetail(paper)
        })
      } else {
        resolve({ code: 2, message: '试卷不存在' })
      }
    }, 300)
  })
}

export const mockExamPaperPageList = (query) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 1,
        response: {
          list: mockExamPaperList,
          total: mockExamPaperList.length,
          pageNum: query && query.pageIndex ? query.pageIndex : 1,
          pageIndex: query && query.pageIndex ? query.pageIndex : 1,
          pageSize: query && query.pageSize ? query.pageSize : 10,
          pages: 1
        }
      })
    }, 300)
  })
}

// ---------- 学科列表（试卷中心左侧 Tab） ----------
export const mockSubjectList = [
  {
    id: 1,
    name: 'Java 开发技术',
    level: 1,
    levelName: '一年级'
  },
  {
    id: 2,
    name: 'Web 前端基础',
    level: 1,
    levelName: '一年级'
  }
]

export const mockSubjectListRequest = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 1,
        response: mockSubjectList
      })
    }, 200)
  })
}
