import type {
  Student, TrainingPlan, InternshipStudent, Contract,
  CooperativeEnterprise, CrmAudit, VisitRecord,
  Warning, WarningStats, EmploymentStats, TrendData,
} from '../types';

export function getMockEmploymentStats(): EmploymentStats {
  return {
    total_graduates: 1200,
    employment_rate: 0.87,
    internship_rate: 0.93,
    flexible_employment_rate: 0.12,
    avg_salary: 9200,
    top_industries: [
      { name: '互联网', ratio: 0.42 },
      { name: '金融', ratio: 0.18 },
      { name: '制造业', ratio: 0.15 },
      { name: '教育', ratio: 0.10 },
      { name: '其他', ratio: 0.15 },
    ],
  };
}

export function getMockTrends(): TrendData {
  return {
    labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
    series: [
      { name: '实习率', data: [0.12, 0.28, 0.55, 0.72, 0.88, 0.93] },
      { name: '三方签约率', data: [0.05, 0.10, 0.22, 0.38, 0.55, 0.68] },
    ],
  };
}

export function getMockStudents(): { total: number; records: Student[] } {
  const classes = ['软工2101', '软工2102', '计科2101', '数据2101'];
  const records: Student[] = Array.from({ length: 20 }, (_, i) => ({
    id: `stu_${String(i + 1).padStart(3, '0')}`,
    student_no: `2021${String(i + 1).padStart(3, '0')}`,
    name: ['张三', '李四', '王五', '赵六', '陈七', '刘八', '孙九', '周十'][i % 8] + (i > 7 ? `${i}` : ''),
    gender: i % 2 === 0 ? 'male' : 'female',
    class_name: classes[i % 4],
    gpa: parseFloat((2.5 + Math.random() * 1.5).toFixed(1)),
    phone: `138${String(10000000 + i).slice(1)}`,
    status: i < 17 ? 'active' : i === 17 ? 'suspended' : 'graduated',
  }));
  return { total: 200, records };
}

export function getMockTrainingPlans(): TrainingPlan[] {
  return [
    {
      id: 'plan_001',
      course_name: 'Java企业级开发实训',
      start_date: '2024-07-01',
      end_date: '2024-07-20',
      target_majors: ['软件工程', '计算机科学'],
      status: 'ongoing',
      credits: 2,
    },
    {
      id: 'plan_002',
      course_name: '前端工程化实训',
      start_date: '2024-08-01',
      end_date: '2024-08-15',
      target_majors: ['软件工程'],
      status: 'published',
      credits: 1,
    },
    {
      id: 'plan_003',
      course_name: '数据分析与可视化',
      start_date: '2024-09-01',
      end_date: '2024-09-20',
      target_majors: ['数据科学'],
      status: 'draft',
      credits: 2,
    },
    {
      id: 'plan_004',
      course_name: 'Python机器学习实训',
      start_date: '2024-06-01',
      end_date: '2024-06-20',
      target_majors: ['数据科学', '计算机科学'],
      status: 'closed',
      credits: 2,
    },
  ];
}

export function getMockInternshipStudents(): { records: InternshipStudent[] } {
  const records: InternshipStudent[] = Array.from({ length: 12 }, (_, i) => ({
    id: `ir_${10000 + i}`,
    studentId: `${1000 + i}`,
    enterpriseId: `${10 + i}`,
    jobId: `${20 + i}`,
    mentorId: `${60 + i}`,
    teacherId: `${30 + i}`,
    startDate: '2024-03-01',
    endDate: i % 3 === 0 ? '2024-06-30' : null,
    status: i % 3 === 0 ? 2 : 1,
    createdAt: '2024-03-01T08:00:00Z',
    updatedAt: '2024-06-30T08:00:00Z',
    studentName: ['张三', '李四', '王五', '赵六', '陈七', '刘八', '孙九', '周十', '吴十一', '郑十二', '冯十三', '蒋十四'][i],
    studentNo: `2021${String(1000 + i).slice(1)}`,
    enterpriseName: ['字节跳动', '腾讯科技', '阿里巴巴', '华为技术', '百度', '美团'][i % 6],
    jobTitle: ['后端开发', '前端开发', '数据分析', '产品经理', '测试工程师'][i % 5],
    mentorName: `导师${i + 1}`,
    teacherName: `指导教师${i + 1}`,
    lastReportTime: i % 5 === 0 ? null : `2024-05-${String(15 + (i % 10)).padStart(2, '0')}T10:00:00Z`,
    statusText: i % 5 === 0 ? 'warning' : (i % 3 === 0 ? 'completed' : 'normal'),
  }));
  return { records };
}

export function getMockContracts() {
  const content = Array.from({ length: 6 }, (_, i) => ({
    id: `contract_${i + 1}`,
    studentName: ['张三', '李四', '王五', '赵六', '陈七', '刘八'][i],
    companyName: ['字节跳动', '腾讯科技', '阿里巴巴', '华为技术', '百度', '美团'][i],
    position: ['后端开发', '前端开发', '数据分析', '产品经理', '测试工程师', '运维工程师'][i],
    submitTime: `2024-05-${String(10 + i).padStart(2, '0')}`,
    status: (i < 3 ? 'pending' : i === 3 ? 'approved' : 'rejected') as Contract['status'],
  }));

  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 10,
    first: true,
    last: true,
  };
}

export function getMockEnterprises(): { records: CooperativeEnterprise[] } {
  const records: CooperativeEnterprise[] = [
    { id: 'ent_001', name: '腾讯科技', industry: '互联网', level: 'strategic', contact_person: '王HR', phone: '13900000001', active_interns: 15, total_hired: 50, status: 'active' },
    { id: 'ent_002', name: '字节跳动', industry: '互联网', level: 'strategic', contact_person: '李HR', phone: '13900000002', active_interns: 20, total_hired: 80, status: 'active' },
    { id: 'ent_003', name: '华为技术', industry: '通信', level: 'core', contact_person: '张HR', phone: '13900000003', active_interns: 8, total_hired: 30, status: 'active' },
    { id: 'ent_004', name: '中国银行', industry: '金融', level: 'core', contact_person: '赵HR', phone: '13900000004', active_interns: 5, total_hired: 20, status: 'active' },
    { id: 'ent_005', name: '本地科技公司', industry: '互联网', level: 'normal', contact_person: '陈HR', phone: '13900000005', active_interns: 2, total_hired: 5, status: 'active' },
  ];
  return { records };
}

export function getMockCrmAudits(): CrmAudit[] {
  return Array.from({ length: 4 }, (_, i) => ({
    id: `audit_${i + 1}`,
    enterprise_name: ['新兴科技', '创业公司A', '数字传媒', '智能制造'][i],
    industry: ['互联网', '互联网', '传媒', '制造业'][i],
    contact_person: `联系人${i + 1}`,
    submit_time: `2024-05-${String(18 + i).padStart(2, '0')}`,
    status: 'pending',
  }));
}

export function getMockVisitRecords(): VisitRecord[] {
  return [
    { id: 'visit_001', enterprise_id: 'ent_001', enterprise_name: '腾讯科技', visit_date: '2024-05-15', visitors: ['张院长', '李辅导员'], content: '洽谈2025届校招合作，确认实习名额50人' },
    { id: 'visit_002', enterprise_id: 'ent_002', enterprise_name: '字节跳动', visit_date: '2024-04-20', visitors: ['王系主任'], content: '参观企业技术中心，了解岗位需求' },
    { id: 'visit_003', enterprise_id: 'ent_003', enterprise_name: '华为技术', visit_date: '2024-03-10', visitors: ['李辅导员', '赵老师'], content: '签署三年战略合作协议' },
  ];
}

export function getMockWarnings(): { records: Warning[] } {
  const records: Warning[] = [
    { id: 'warn_001', student_id: 'stu_001', student_name: '赵六', type: 'attendance_abnormal', level: 'high', description: '连续3天未在实习地打卡，且未提交请假条', trigger_time: '2024-05-24 10:00:00', status: 'unhandled' },
    { id: 'warn_002', student_id: 'stu_002', student_name: '陈七', type: 'missing_report', level: 'medium', description: '连续2周未提交实习周报', trigger_time: '2024-05-22 09:00:00', status: 'handling' },
    { id: 'warn_003', student_id: 'stu_003', student_name: '刘八', type: 'unemployed', level: 'low', description: '距毕业还有3个月，尚未找到实习单位', trigger_time: '2024-05-20 08:00:00', status: 'unhandled' },
    { id: 'warn_004', student_id: 'stu_004', student_name: '孙九', type: 'attendance_abnormal', level: 'high', description: '实习地点与协议不符，疑似擅自离岗', trigger_time: '2024-05-23 14:00:00', status: 'unhandled' },
    { id: 'warn_005', student_id: 'stu_005', student_name: '周十', type: 'missing_report', level: 'medium', description: '本周周报内容过于简短，疑似敷衍', trigger_time: '2024-05-21 16:00:00', status: 'resolved' },
  ];
  return { records };
}

export function getMockWarningStats(): WarningStats {
  return {
    high_count: 5,
    medium_count: 12,
    low_count: 30,
    type_distribution: {
      missing_report: 20,
      attendance: 15,
      safety: 2,
    },
  };
}
