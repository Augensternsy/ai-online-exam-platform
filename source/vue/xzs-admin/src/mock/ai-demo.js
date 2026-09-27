/**
 * Mock AI 出题数据
 */

export const mockAiDemoQuestions = [
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

export const mockAiGenerate = (input) => {
  return new Promise((resolve) => {
    // 模拟 1.5 秒 AI 生成延迟
    setTimeout(() => {
      resolve({
        code: 1,
        response: {
          totalCount: mockAiDemoQuestions.length,
          qualifiedCount: mockAiDemoQuestions.length,
          rejectedCount: 0,
          processingTime: 1500,
          markdownContent: `# AI 生成结果\n\n> 输入需求：${input}\n\n共生成 ${mockAiDemoQuestions.length} 道试题，包含单选题、多选题和判断题。`,
          qualifiedQuestions: mockAiDemoQuestions,
          rejectedQuestions: []
        }
      })
    }, 1500)
  })
}

export const mockAiGenerateByText = (input) => {
  return new Promise((resolve) => {
    const start = Date.now()
    setTimeout(() => {
      resolve({
        code: 1,
        response: {
          requirement: {
            subject: 'Java',
            difficulty: 2,
            questionCount: mockAiDemoQuestions.length,
            questionTypes: ['single', 'multiple', 'judge']
          },
          steps: [
            {
              name: '需求解析',
              detail: '识别学科=Java、难度=中等、题量=' + mockAiDemoQuestions.length + '、题型=单选/多选/判断',
              costMs: 320
            },
            {
              name: '规划方案',
              detail: '基于 Spring Boot 核心知识点制定出题方案',
              costMs: 260
            },
            {
              name: '执行生成',
              detail: '调用 DeepSeek 模型生成结构化题目',
              costMs: 1180
            },
            {
              name: '校验评估',
              detail: 'JSON 格式校验与质量评分，' + mockAiDemoQuestions.length + ' 道题全部合格',
              costMs: 240
            }
          ],
          totalCount: mockAiDemoQuestions.length,
          qualifiedCount: mockAiDemoQuestions.length,
          rejectedCount: 0,
          processingTime: Date.now() - start,
          markdownContent: `# AI 生成结果\n\n> 输入需求：${input}\n\n共生成 ${mockAiDemoQuestions.length} 道试题，包含单选题、多选题和判断题。`,
          qualifiedQuestions: mockAiDemoQuestions,
          rejectedQuestions: []
        }
      })
    }, 2000)
  })
}
