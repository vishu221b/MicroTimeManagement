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

function App() {
  return (
    <>
      <NavigationBar />
      <Routes>
        <Route path={"/"} element={<Home />}></Route>
        <Route path={"/login"} element={<Login />}></Route>
        <Route path={"/register"} element={<Registration />}></Route>
      </Routes>
      <Footer />
    </>
  );
}

export default App;
