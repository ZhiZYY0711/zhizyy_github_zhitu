import type {
  DashboardStats, Job, Application, TalentPoolItem, TrainingProject,
  ProjectTeam, Intern, WeeklyReport, CodeReview, MentorDashboard, AnalyticsData,
  TodoItem, ActivityItem,
} from '../types';

const ri = (min: number, max: number) => Math.floor(Math.random() * (max - min + 1)) + min;
const rf = (min: number, max: number) => parseFloat((Math.random() * (max - min) + min).toFixed(2));
const pick = <T>(arr: readonly T[]): T => arr[Math.floor(Math.random() * arr.length)];
const uid = () => Math.random().toString(36).slice(2, 10);
const pastDate = (daysAgo: number) => new Date(Date.now() - daysAgo * 86400_000).toISOString().slice(0, 10);
const futureDate = (daysAhead: number) => new Date(Date.now() + daysAhead * 86400_000).toISOString().slice(0, 10);

const SCHOOLS = ['北京大学', '清华大学', '复旦大学', '浙江大学', '上海交通大学', '南京大学', '武汉大学', '中山大学'];
const MAJORS = ['计算机科学', '软件工程', '信息工程', '数据科学', '人工智能', '网络工程'];
const STUDENT_NAMES = ['张三', '李四', '王五', '赵六', '陈七', '刘八', '周九', '吴十', '郑十一', '孙十二'];
const TECH_STACKS = ['React', 'Vue', 'Spring Boot', 'Node.js', 'Python', 'Redis', 'MySQL', 'Docker', 'Kubernetes', 'TypeScript'];
const DEPARTMENTS = ['前端组', '后端组', '算法组', '测试组', '运维组'];

export const getMockDashboardStats = (): DashboardStats => ({
  active_jobs: ri(5, 20),
  pending_applications: ri(10, 50),
  active_interns: ri(5, 30),
  pending_reviews: ri(2, 15),
  pending_weekly_reports: ri(3, 12),
  upcoming_interviews: ri(1, 8),
});

export const getMockJobs = (): Job[] => [
  { id: 'job_001', title: 'Java 后端实习生', type: 'internship', description: '参与核心业务系统开发', requirements: ['熟悉 Spring Boot', '了解 MySQL', '有 Java 基础'], salary_range: '200-300/天', location: '北京', status: 'active', created_at: pastDate(10), updated_at: pastDate(2), applicant_count: 23 },
  { id: 'job_002', title: '前端开发实习生', type: 'internship', description: '参与 Web 前端开发', requirements: ['熟悉 React 或 Vue', '了解 TypeScript', '有项目经验'], salary_range: '180-280/天', location: '上海', status: 'active', created_at: pastDate(15), updated_at: pastDate(1), applicant_count: 31 },
  { id: 'job_003', title: '算法工程师实习生', type: 'internship', description: '参与推荐算法研发', requirements: ['熟悉 Python', '了解机器学习', '有数学基础'], salary_range: '250-350/天', location: '北京', status: 'active', created_at: pastDate(5), updated_at: pastDate(1), applicant_count: 18 },
  { id: 'job_004', title: '测试工程师实习生', type: 'internship', description: '参与自动化测试开发', requirements: ['了解测试理论', '熟悉 Python 或 Java'], salary_range: '150-250/天', location: '杭州', status: 'closed', created_at: pastDate(30), updated_at: pastDate(5), applicant_count: 12 },
  { id: 'job_005', title: '数据分析实习生', type: 'internship', description: '参与数据分析与可视化', requirements: ['熟悉 SQL', '了解 Python 数据分析', '有统计学基础'], salary_range: '200-300/天', location: '北京', status: 'active', created_at: pastDate(8), updated_at: pastDate(1), applicant_count: 27 },
];

export const getMockApplications = (): Application[] =>
  Array.from({ length: 12 }, (_, i) => ({
    application_id: `app_${String(i + 1).padStart(3, '0')}`,
    job_id: pick(['job_001', 'job_002', 'job_003', 'job_005']),
    job_title: pick(['Java 后端实习生', '前端开发实习生', '算法工程师实习生', '数据分析实习生']),
    student_id: `stu_${uid()}`,
    student_name: pick(STUDENT_NAMES),
    school: pick(SCHOOLS),
    major: pick(MAJORS),
    resume_url: `https://oss.example.com/resumes/resume_${i + 1}.pdf`,
    match_score: rf(0.3, 0.98),
    apply_time: pastDate(ri(1, 20)),
    status: pick(['pending', 'pending', 'pending', 'interview', 'offered', 'rejected'] as const),
    interview_time: i % 3 === 0 ? futureDate(ri(1, 7)) : undefined,
    interview_type: i % 3 === 0 ? pick(['online', 'onsite'] as const) : undefined,
  }));

export const getMockTalentPool = (): TalentPoolItem[] =>
  Array.from({ length: 10 }, (_, i) => ({
    id: `tp_${uid()}`,
    student_id: `stu_${uid()}`,
    student_name: pick(STUDENT_NAMES),
    school: pick(SCHOOLS),
    major: pick(MAJORS),
    tags: Array.from({ length: ri(2, 4) }, () => pick(['潜力股', '25届', '26届', '技术强', '沟通好', '有实习经验', '竞赛获奖'])),
    skills: Array.from({ length: ri(3, 5) }, () => pick(TECH_STACKS)),
    collect_time: pastDate(ri(5, 60)),
    notes: i % 3 === 0 ? '面试表现优秀，技术扎实，建议优先考虑' : undefined,
  }));

export const getMockTrainingProjects = (): TrainingProject[] => [
  { id: 'proj_001', name: '电商高并发秒杀系统', description: '基于真实业务场景，实现高并发秒杀功能', difficulty: 4, tech_stack: ['Spring Cloud', 'Redis', 'RocketMQ', 'MySQL'], max_teams: 10, current_teams: 7, status: 'in_progress', start_date: pastDate(30), end_date: futureDate(30), created_at: pastDate(45) },
  { id: 'proj_002', name: '智能推荐系统', description: '基于协同过滤算法实现个性化推荐', difficulty: 5, tech_stack: ['Python', 'TensorFlow', 'Kafka', 'Elasticsearch'], max_teams: 6, current_teams: 4, status: 'recruiting', created_at: pastDate(10) },
  { id: 'proj_003', name: '微服务网关设计', description: '设计并实现企业级 API 网关', difficulty: 3, tech_stack: ['Spring Cloud Gateway', 'Redis', 'Docker'], max_teams: 8, current_teams: 8, status: 'in_progress', start_date: pastDate(20), end_date: futureDate(40), created_at: pastDate(35) },
  { id: 'proj_004', name: '数据可视化平台', description: '构建企业数据分析与可视化平台', difficulty: 2, tech_stack: ['React', 'TypeScript', 'ECharts', 'Node.js'], max_teams: 5, current_teams: 3, status: 'recruiting', created_at: pastDate(5) },
];

export const getMockProjectTeams = (projectId: string): ProjectTeam[] =>
  Array.from({ length: ri(2, 4) }, (_, i) => ({
    team_id: `team_${projectId}_${i + 1}`,
    project_id: projectId,
    team_name: `第${i + 1}组`,
    members: Array.from({ length: ri(3, 5) }, (_, j) => ({
      student_id: `stu_${uid()}`,
      student_name: pick(STUDENT_NAMES),
      school: pick(SCHOOLS),
      role: j === 0 ? 'leader' as const : 'member' as const,
    })),
    mentor_id: i % 2 === 0 ? 'mentor_001' : undefined,
    mentor_name: i % 2 === 0 ? '李技术' : undefined,
    progress: ri(10, 90),
    status: 'active',
  }));

export const getMockInterns = (): Intern[] =>
  Array.from({ length: 8 }, (_, i) => ({
    id: `intern_${String(i + 1).padStart(3, '0')}`,
    student_id: `stu_${uid()}`,
    name: pick(STUDENT_NAMES),
    school: pick(SCHOOLS),
    major: pick(MAJORS),
    position: pick(['Java 后端实习生', '前端开发实习生', '算法工程师实习生', '数据分析实习生']),
    start_date: pastDate(ri(10, 90)),
    end_date: i > 5 ? pastDate(ri(1, 10)) : undefined,
    mentor_id: pick(['mentor_001', 'mentor_002']),
    mentor_name: pick(['李技术', '王导师']),
    status: i > 5 ? pick(['completed', 'terminated'] as const) : 'active',
    contract_status: 'signed',
    salary: `${pick(['200', '250', '280', '300'])}/天`,
  }));

export const getMockWeeklyReports = (): WeeklyReport[] =>
  Array.from({ length: 10 }, (_, i) => ({
    id: `report_${String(i + 1).padStart(3, '0')}`,
    intern_id: `intern_${String((i % 8) + 1).padStart(3, '0')}`,
    intern_name: pick(STUDENT_NAMES),
    week: ri(1, 12),
    content: `本周主要完成了${pick(['用户模块', '订单模块', '支付模块', '数据分析', '接口联调'])}的开发工作，遇到了${pick(['并发问题', '性能瓶颈', '接口设计', '数据库优化'])}，通过${pick(['查阅文档', '请教导师', '团队讨论'])}解决了问题。下周计划继续推进${pick(['测试工作', '功能开发', '代码优化', '文档编写'])}。`,
    submit_time: pastDate(ri(1, 14)),
    score: i % 3 !== 0 ? ri(70, 98) : undefined,
    mentor_comment: i % 3 !== 0 ? pick(['进度符合预期，继续保持', '代码质量不错，注意测试覆盖率', '本周表现优秀，下周注意并发问题']) : undefined,
    status: i % 3 !== 0 ? 'reviewed' : 'pending',
  }));

export const getMockCodeReviews = (): CodeReview[] => [
  { id: 'cr_001', project_id: 'proj_001', project_name: '电商高并发秒杀系统', student_id: 'stu_001', student_name: '张三', file: 'UserService.java', line: 45, code_snippet: 'for(User u : users) { db.save(u); }', comment: '', status: 'pending', created_at: pastDate(2) },
  { id: 'cr_002', project_id: 'proj_001', project_name: '电商高并发秒杀系统', student_id: 'stu_002', student_name: '李四', file: 'OrderController.java', line: 78, code_snippet: 'if(stock > 0) { stock--; order.save(); }', comment: '', status: 'pending', created_at: pastDate(1) },
  { id: 'cr_003', project_id: 'proj_003', project_name: '微服务网关设计', student_id: 'stu_003', student_name: '王五', file: 'GatewayFilter.java', line: 23, code_snippet: 'String token = request.getHeader("token");', comment: '建议使用 Authorization 标准头', status: 'resolved', created_at: pastDate(5), resolved_at: pastDate(3) },
  { id: 'cr_004', project_id: 'proj_003', project_name: '微服务网关设计', student_id: 'stu_004', student_name: '赵六', file: 'RateLimiter.java', line: 56, code_snippet: 'Thread.sleep(1000);', comment: '', status: 'pending', created_at: pastDate(1) },
  { id: 'cr_005', project_id: 'proj_001', project_name: '电商高并发秒杀系统', student_id: 'stu_005', student_name: '陈七', file: 'CacheService.java', line: 112, code_snippet: 'cache.put(key, value);', comment: '需要设置过期时间', status: 'resolved', created_at: pastDate(7), resolved_at: pastDate(4) },
];

export const getMockMentorDashboard = (): MentorDashboard => ({
  pending_code_reviews: ri(2, 8),
  pending_weekly_reports: ri(1, 6),
  upcoming_interviews: ri(0, 3),
  students: Array.from({ length: ri(4, 8) }, (_, i) => ({
    student_id: `stu_${uid()}`,
    student_name: pick(STUDENT_NAMES),
    type: i % 3 === 0 ? 'internship' : 'training',
    project_name: i % 3 !== 0 ? pick(['电商高并发秒杀系统', '微服务网关设计', '数据可视化平台']) : undefined,
    position: i % 3 === 0 ? pick(['Java 后端实习生', '前端开发实习生']) : undefined,
    start_date: pastDate(ri(10, 60)),
  })),
});

export const getMockAnalytics = (): AnalyticsData => ({
  conversion_rate: {
    internship_to_fulltime: rf(0.3, 0.7),
    cost_saving: ri(50000, 200000),
  },
  conversion_trend: ['1月', '2月', '3月', '4月', '5月', '6月'].map(month => ({
    month,
    rate: rf(0.25, 0.75),
  })),
  contribution: {
    total_value: ri(500000, 2000000),
    by_department: DEPARTMENTS.map(department => ({
      department,
      value: ri(50000, 400000),
    })),
  },
  recruitment_funnel: [
    { stage: '简历投递', count: ri(200, 500) },
    { stage: '简历筛选', count: ri(80, 150) },
    { stage: '面试邀约', count: ri(40, 80) },
    { stage: '发送 Offer', count: ri(15, 40) },
    { stage: '成功入职', count: ri(8, 20) },
  ],
});

export const getMockTodos = (): TodoItem[] => [
  { id: 'todo_001', type: 'interview', title: '面试：张三 - Java 后端实习生', description: '明天 14:00 在线面试', due_time: futureDate(1), priority: 'high' },
  { id: 'todo_002', type: 'weekly_report', title: '批阅周报：李四 第3周', description: '已提交 2 天，待批阅', priority: 'medium' },
  { id: 'todo_003', type: 'code_review', title: '代码评审：UserService.java', description: '电商秒杀系统 - 张三', priority: 'high' },
  { id: 'todo_004', type: 'contract', title: '合同待签：王五 实习协议', description: '学生已签署，等待企业确认', priority: 'medium' },
  { id: 'todo_005', type: 'weekly_report', title: '批阅周报：赵六 第5周', description: '已提交 1 天，待批阅', priority: 'low' },
];

export const getMockActivities = (): ActivityItem[] => [
  { id: 'act_001', type: 'application', title: '新简历投递', description: '陈七 投递了 前端开发实习生', time: pastDate(0), actor: '陈七' },
  { id: 'act_002', type: 'interview', title: '面试已安排', description: '张三 的面试已安排至明天 14:00', time: pastDate(1), actor: '张经理' },
  { id: 'act_003', type: 'weekly_report', title: '周报已提交', description: '李四 提交了第 3 周周报', time: pastDate(2), actor: '李四' },
  { id: 'act_004', type: 'review', title: '代码评审请求', description: '王五 提交了 UserService.java 的评审请求', time: pastDate(2), actor: '王五' },
  { id: 'act_005', type: 'application', title: '新简历投递', description: '刘八 投递了 算法工程师实习生', time: pastDate(3), actor: '刘八' },
];
