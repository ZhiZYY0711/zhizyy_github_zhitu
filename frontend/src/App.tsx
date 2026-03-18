import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/context';
import ProtectedRoute from './auth/ProtectedRoute';
import LoginPage from './login/page';
import StudentLayout from './student/layout';
import StudentDashboard from './student/dashboard/page';
import TrainingPage from './student/training/page';
import InternshipPage from './student/internship/page';
import GrowthPage from './student/growth/page';

// Enterprise Module
import EnterpriseLayout from './enterprise/layout';
import EnterpriseDashboard from './enterprise/dashboard/page';
import RecruitmentPage from './enterprise/recruitment/page';
import TalentPoolPage from './enterprise/talent-pool/page';
import EnterpriseTrainingPage from './enterprise/training/page';
import EnterpriseInternshipPage from './enterprise/internship/page';
import MentorPage from './enterprise/mentor/page';
import AnalyticsPage from './enterprise/analytics/page';

// College Module
import CollegeLayout from './college/layout';
import CollegeDashboard from './college/dashboard/page';
import TeachingPage from './college/teaching/page';
import EmploymentPage from './college/employment/page';
import CrmPage from './college/crm/page';
import WarningPage from './college/warning/page';

// Platform Module
import PlatformLayout from './platform/layout';
import PlatformDashboard from './platform/dashboard/page';
import MasterDataPage from './platform/master-data/page';
import AuditPage from './platform/audit/page';
import ResourcesPage from './platform/resources/page';
import MonitorPage from './platform/monitor/page';
import LogsPage from './platform/logs/page';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* Student Module Routes */}
          <Route path="/student" element={<ProtectedRoute><StudentLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/student/dashboard" replace />} />
            <Route path="dashboard" element={<StudentDashboard />} />
            <Route path="training" element={<TrainingPage />} />
            <Route path="internship" element={<InternshipPage />} />
            <Route path="growth" element={<GrowthPage />} />
          </Route>

          {/* Enterprise Module Routes */}
          <Route path="/enterprise" element={<ProtectedRoute><EnterpriseLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/enterprise/dashboard" replace />} />
            <Route path="dashboard" element={<EnterpriseDashboard />} />
            <Route path="recruitment" element={<RecruitmentPage />} />
            <Route path="talent-pool" element={<TalentPoolPage />} />
            <Route path="training" element={<EnterpriseTrainingPage />} />
            <Route path="internship" element={<EnterpriseInternshipPage />} />
            <Route path="mentor" element={<MentorPage />} />
            <Route path="analytics" element={<AnalyticsPage />} />
          </Route>

          {/* College Module Routes */}
          <Route path="/college" element={<ProtectedRoute><CollegeLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/college/dashboard" replace />} />
            <Route path="dashboard" element={<CollegeDashboard />} />
            <Route path="teaching" element={<TeachingPage />} />
            <Route path="employment" element={<EmploymentPage />} />
            <Route path="crm" element={<CrmPage />} />
            <Route path="warning" element={<WarningPage />} />
          </Route>

          {/* Platform Module Routes */}
          <Route path="/platform" element={<ProtectedRoute><PlatformLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/platform/dashboard" replace />} />
            <Route path="dashboard" element={<PlatformDashboard />} />
            <Route path="master-data" element={<MasterDataPage />} />
            <Route path="audit" element={<AuditPage />} />
            <Route path="resources" element={<ResourcesPage />} />
            <Route path="monitor" element={<MonitorPage />} />
            <Route path="logs" element={<LogsPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
