/**
 * AI 出题离线 Demo 数据
 * 演示主题：Java Spring Boot 初级开发工程师考试
 * 结构与后端 /api/admin/ai-question/generate 的返回 response 完全一致，
 * 用于公网环境 AI 接口不稳定时保证演示流程可走通（查看结果 -> 保存题库）。
 */
export const DEMO_INPUT = 'Java Spring Boot 初级开发工程师考试'

const demoQuestions = [
  {
    questionType: 1,
    knowledge_point: 'Spring Boot 核心',
    question: 'Spring Boot 项目的启动类通常使用以下哪个注解？',
    options: ['@SpringBootApplication', '@Configuration', '@ComponentScan', '@EnableScheduling'],
    correct_answer: 'A',
    analysis: '@SpringBootApplication 是核心组合注解，包含 @SpringBootConfiguration、@EnableAutoConfiguration 和 @ComponentScan。',
    difficulty: 1,
    qualityScore: 4.8
  },
  {
    questionType: 1,
    knowledge_point: 'Spring Boot 核心',
    question: '在未手动替换的情况下，Spring Boot 默认内嵌的 Web 容器是？',
    options: ['Jetty', 'Tomcat', 'Undertow', 'Netty'],
    correct_answer: 'B',
    analysis: 'spring-boot-starter-web 默认引入 Tomcat；如需 Jetty 或 Undertow，需要排除 Tomcat 后手动引入。',
    difficulty: 1,
    qualityScore: 4.7
  },
  {
    questionType: 1,
    knowledge_point: '配置文件',
    question: '在 application.yml 中要将服务端口修改为 8081，正确的写法是？',
    options: ['server.port: 8081', 'spring.port: 8081', 'http.port: 8081', 'port: 8081'],
    correct_answer: 'A',
    analysis: '服务端口通过 server.port 属性配置，该属性作用于内嵌 Web 容器。',
    difficulty: 1,
    qualityScore: 4.6
  },
  {
    questionType: 1,
    knowledge_point: '分层注解',
    question: '下列注解中，通常用于标注业务逻辑层（Service 层）组件的是？',
    options: ['@Service', '@Controller', '@RestController', '@Configuration'],
    correct_answer: 'A',
    analysis: '@Service 用于标注业务逻辑层组件，会被 Spring 容器扫描并注册为 Bean。',
    difficulty: 1,
    qualityScore: 4.5
  },
  {
    questionType: 2,
    knowledge_point: 'Spring Boot 特性',
    question: '下列属于 Spring Boot 主要优点的有？',
    options: ['自动配置，减少繁琐的 XML 配置', '内嵌 Web 容器，开箱即用', '提供 starter 起步依赖，简化构建', '只能在 Linux 操作系统上运行'],
    correct_answer: 'A,B,C',
    analysis: '自动配置、内嵌容器、starter 依赖是 Spring Boot 的核心优势；它不限制操作系统。',
    difficulty: 2,
    qualityScore: 4.7
  },
  {
    questionType: 2,
    knowledge_point: '依赖注入',
    question: 'Spring 中常见的依赖注入方式包括下列哪些？',
    options: ['构造方法注入', 'Setter 方法注入', '字段注入（@Autowired）', '在类中使用 new 直接创建对象'],
    correct_answer: 'A,B,C',
    analysis: 'Spring 支持构造器注入、Setter 注入和字段注入；new 创建的对象不受 Spring 容器管理，不属于依赖注入。',
    difficulty: 2,
    qualityScore: 4.6
  },
  {
    questionType: 3,
    knowledge_point: 'Web 注解',
    question: '在控制器类上使用 @RestController，其效果等价于同时使用 @Controller 和 @ResponseBody。',
    options: ['正确', '错误'],
    correct_answer: 'A',
    analysis: '@RestController 是 @Controller 与 @ResponseBody 的组合注解，方法返回值默认直接序列化为 JSON 写入响应体。',
    difficulty: 1,
    qualityScore: 4.8
  },
  {
    questionType: 3,
    knowledge_point: '依赖注入',
    question: '@Autowired 注解只能按照 Bean 的名称进行注入，无法按类型注入。',
    options: ['正确', '错误'],
    correct_answer: 'B',
    analysis: '@Autowired 默认按类型（byType）注入，配合 @Qualifier 注解时可以按名称指定具体的 Bean。',
    difficulty: 2,
    qualityScore: 4.5
  }
]

export function buildDemoResult () {
  return {
    totalCount: demoQuestions.length,
    qualifiedCount: demoQuestions.length,
    rejectedCount: 0,
    processingTime: 2680,
    markdownContent: `# ${DEMO_INPUT}\n\n> 本结果为内置 Demo 数据，用于无公网 / AI 接口不可用时的功能演示。\n\n共生成 ${demoQuestions.length} 道试题，包含单选题、多选题和判断题。`,
    qualifiedQuestions: demoQuestions,
    rejectedQuestions: []
  }
}
