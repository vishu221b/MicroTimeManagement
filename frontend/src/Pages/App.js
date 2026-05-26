import NavigationBar from "../components/NavigationBar";
import "bootstrap/dist/css/bootstrap.min.css";
import "antd/dist/reset.css";
import "../style/tailwind.css";
import "../style/App.css";
import Home from "./Home";
import Footer from "../components/Footer";
import { Route, Routes } from "react-router-dom";
import Login from "./Login";
import Registration from "./Registration";
import Dashboard from "./Dashboard";
import Activity from "./Activity";
import Profile from "./Profile";
import Admin from "./Admin";
import ProtectedRoute from "../components/ProtectedRoute";
import AdminRoute from "../components/AdminRoute";
import { useState } from "react";
import Toast from "../components/Toast";

function App() {
  let defaultToastState = {
    display: false,
    variant: "",
    messages: [],
    includePrefix: false,
    includeSuffix: false,
    suffix: "",
  };
  const [toastState, setToastState] = useState(defaultToastState);
  return (
    <>
      <NavigationBar />
      <div className="mtm-z-40 mtm-w-[60%] sm:mtm-w-[45%] md:mtm-w-[35%] lg:mtm-w-[25%] mtm-fixed mtm-right-1 mtm-flex mtm-flex-col mtm-pr-0">
        {toastState.display ? (
          toastState.messages.map((message, index) => (
            <Toast
              variant={toastState.variant}
              key={index}
              show={true}
              autoHide
              autoHideDelayInMs={5000}
              includePrefix={toastState.variant === "success" ? true : false}
              includeSuffix={toastState.includeSuffix}
              suffix={toastState.suffix}
            >
              {message}
            </Toast>
          ))
        ) : (
          <></>
        )}
      </div>
      <Routes>
        <Route path={"/"} element={<Home />}></Route>
        <Route
          path={"/login"}
          element={
            <Login toastState={toastState} setToastState={setToastState} />
          }
        ></Route>
        <Route
          path={"/register"}
          element={
            <Registration
              toastState={toastState}
              setToastState={setToastState}
            />
          }
        ></Route>
        <Route
          path={"/dashboard"}
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        ></Route>
        <Route
          path={"/activity"}
          element={
            <ProtectedRoute>
              <Activity
                toastState={toastState}
                setToastState={setToastState}
              />
            </ProtectedRoute>
          }
        ></Route>
        <Route
          path={"/profile"}
          element={
            <ProtectedRoute>
              <Profile
                toastState={toastState}
                setToastState={setToastState}
              />
            </ProtectedRoute>
          }
        ></Route>
        <Route
          path={"/admin"}
          element={
            <AdminRoute>
              <Admin
                toastState={toastState}
                setToastState={setToastState}
              />
            </AdminRoute>
          }
        ></Route>
      </Routes>
      <Footer />
    </>
  );
}

export default App;
