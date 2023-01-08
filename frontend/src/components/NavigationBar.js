import React, { useState } from "react";
import mtm from "../Micro.gif";
import { Link } from "react-router-dom";
import Nav from "react-bootstrap/Nav";
import Navbar from "react-bootstrap/Navbar";
import { Button } from "react-bootstrap";
import { AiOutlineAlignCenter } from "react-icons/ai";
import { BsCaretUpFill } from "react-icons/bs";
import Home from "../Pages/Home";

const twButton =
  "mtm-animate-pulse mtm-text-white mtm-w-[130px] mtm-mt-[1%] sm:mtm-my-[0px] hover:mtm-bg-yellow-300 hover:mtm-text-black hover:mtm-animate-none";

function NavigationBar() {
  const [toggled, setToggled] = useState(0);
  return (
    <Navbar
      sticky="top"
      collapseOnSelect
      expand="md"
      //   bg="dark"
      //   variant="dark"
      className="mtm-bg-black mtm-shadow-md mtm-shadow-cyan-800 mtm-px-5 mtm-font-sans"
      onToggle={(val) => setToggled(val)}
    >
      <Navbar.Brand className="hover:animate-bounce">
        <Link to={"/"} preventScrollReset={false}>
          <img src={mtm} width={200} alt="" />
        </Link>
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="responsive-navbar-nav">
        {toggled ? (
          <BsCaretUpFill
            className="mtm-bg-yeallow-100 mtm-rounded-lg m-1 mtm-min-w-[40px] mtm-min-h-[33px] mtm-animate-bounce"
            size={23}
            color={"#6DE7B2"}
          />
        ) : (
          <AiOutlineAlignCenter
            className="mtm-bg-yeallow-100 mtm-rounded-lg m-1 mtm-min-w-[40px] mtm-min-h-[33px] mtm-animate-none"
            size={33}
            color={"#6DE7B2"}
          />
        )}
      </Navbar.Toggle>
      <Navbar.Collapse id="responsive-navbar-nav">
        <Nav className="mtm-ml-auto mtm-py-10 sm:mtm-py-0">
          <Nav.Link
            eventKey={2}
            className="mtm-mx-auto mtm-my-4 md:mtm-my-0"
            as={"div"}
          >
            <Link to={"/login"} preventScrollReset>
              <Button variant="warning" size="md" className={twButton}>
                <span className="mtm-tracking-widest">Sign in</span>
              </Button>
            </Link>
          </Nav.Link>
          <Nav.Link
            eventKey={2}
            className="mtm-mx-auto mtm-my-4 md:mtm-my-0"
            as={"div"}
          >
            <Link to={"/register"}>
              <Button variant="warning" size="md" className={twButton}>
                <span className="mtm-tracking-widest">Try Now!</span>
              </Button>
            </Link>
          </Nav.Link>
        </Nav>
      </Navbar.Collapse>
    </Navbar>
  );
}

export default NavigationBar;
