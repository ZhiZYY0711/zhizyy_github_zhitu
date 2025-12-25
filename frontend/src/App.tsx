import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import StudentLayout from './student/layout';
import StudentDashboard from './student/dashboard/page';
import TrainingPage from './student/training/page';
import InternshipPage from './student/internship/page';
import GrowthPage from './student/growth/page';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/student/dashboard" replace />} />

        {/* Student Module Routes */}
        <Route path="/student" element={<StudentLayout />}>
          <Route index element={<Navigate to="/student/dashboard" replace />} />
          <Route path="dashboard" element={<StudentDashboard />} />
          <Route path="training" element={<TrainingPage />} />
          <Route path="internship" element={<InternshipPage />} />
          <Route path="growth" element={<GrowthPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
