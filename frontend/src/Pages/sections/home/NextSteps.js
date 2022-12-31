import React, { useState } from "react";
import { Button, Card, Container, Nav, Tab } from "react-bootstrap";
import { Tabs } from "antd";
import SingleMeme from "../../../components/SingleMeme";
import dwight from "../../../dwight.gif";

function NextSteps() {
  const [navKey, setNavKey] = useState("#Register");
  return (
    <div
      className="mtm-bg-cover mtm-bg-center"
      style={{
        backgroundImage:
          "url('https://lookhere3.files.wordpress.com/2016/02/spinning-clock.gif')",
      }}
    >
      <Container className="mtm-min-w-full mtm-bg-black/90 mtm-p-10 mtm-min-h-screen mtm-text-white/90">
        <div
          className="
        mtm-text-5xl 
        mtm-text-center 
        mtm-font-sans 
        mtm-tracking-widest 
        mtm-text-yellow-500 
        mtm-pb-5
        mtm-underline
        "
          style={{ textShadow: "0px 0px 40px white" }}
        >
          Next Steps
        </div>
        <hr />
        <Card className="mtm-mt-10 mtm-text-white/90 mtm-p-10 mtm-text-center mtm-bg-white/5 mtm-font-bal mtm-text-2xl">
          <Card.Header
            style={{ borderBottom: "1px solid grey" }}
            className="mtm-py-[4%]"
          >
            <Card.Title className="mtm-text-2xl md:mtm-text-3xl mtm-my-3">
              First of all, get started by{" "}
              <span className="mtm-text-green-500 hover:mtm-text-green-400 mtm-font-sans mtm-tracking-wider mtm-underline">
                {"creating a free account"}
              </span>
              <br />
              <br />
              <SingleMeme
                memeSrc={
                  "https://media1.giphy.com/media/sl1zfWPqlozOgquzuE/giphy.gif"
                }
              />
              <br />
              <Button
                className="
                mtm-text-2xl 
                mtm-mt-[0%] 
                mtm-py-[1.5%] 
                mtm-font-sans 
                mtm-tracking-widest 
                btn-outline-warning 
                mtm-animate-pulse 
                mtm-px-10 
            "
                style={{
                  boxShadow: "0px 0px 25px 1px green",
                }}
              >
                <span className="mtm-text-white hover:mtm-text-black active:mtm-text-black mtm-text-2xl">
                  Create free Account!!
                </span>
              </Button>
            </Card.Title>
            {/* <div className="mtm-text-yellow-500">
              <Nav
                fill
                variant="tabs"
                defaultActiveKey={navKey}
                className="justify-content-center mtm-border-0 mtm-border-yellow-500 mtm-rounded-lg mtm-pt-3 mtm-min-w-[50%] lg:mtm-max-w-[50%] mtm-mx-auto"
                style={{ borderBottom: "1px solig white" }}
              >
                <Nav.Item className="hover:mtm-bg-ayellow-600 mtm-bg-yellow-a300 btn-success-outline">
                  <Nav.Link
                    href="#Register"
                    onClick={() => setNavKey("#Register")}
                    className="mtm-border-2"
                  >
                    <span className="mtm-text-green-500 active:mtm-text-green-900">
                      Register
                    </span>
                  </Nav.Link>
                </Nav.Item>
                <Nav.Item className="mx-0 matm-bg-red-500">
                  <Nav.Link
                    href="#Login"
                    onClick={() => setNavKey("#Login")}
                    className="mtm-bg-red-600 hover:mtm-bg-red-700 active:mtm-bg-red-900"
                    style={
                      {
                        //   backgroundColor: "red",
                      }
                    }
                  >
                    <span className="mtm-text-green-500 active:mtm-text-green-900">
                      Sign In
                    </span>
                  </Nav.Link>
                </Nav.Item>
              </Nav>
            </div> */}
          </Card.Header>
          {/* <div className="mtm-min-w-[100%] sm:mtm-min-w-[70%] lg:mtm-min-w-[50%] mtm-mx-auto mtm-bg-white mtm-rounded-b-xl">
            <Card.Body
              id="Register"
              className={navKey == "#Register" ? "d-block" : "d-none"}
              style={{
                borderTop: "0px solid grey",
                marginTop: 0,
              }}
            >
              Body Register
            </Card.Body>
            <Card.Body
              id="#Login"
              className={navKey == "#Login" ? "d-block" : "d-none"}
              style={{
                borderTop: "0px solid grey",
                marginTop: 0,
              }}
            >
              Body one
            </Card.Body>
          </div> */}
          <Card.Body className="mtm-font-bal mtm-tracking-wider mtm-my-10 mtm-text-xl md:mtm-text-2xl mtm-text-center">
            {/* Basically, you start by logging your daily activities, for the
            current date respectively, into the app. Later on, you review your
            logged activities and see how much time you've spent doing what.
            <br />
            <br />
            <br /> */}
            <span className="mtm-text-2xl md:mtm-text-3xl">
              Now, start{" "}
              <span className="mtm-text-yellow-500 mtm-font-bold">
                using Micro Time Management{" "}
              </span>
              to:
            </span>
            <br />
            <br />
            <ul
              className="
            mtm-leading-10 mtm-text-justify mtm-mx-auto md:mtm-max-w-[53%] mtm-list-['(+)'] mtm-list-inside mtm-text-yellow-500 mtm-font-code"
            >
              <li>
                <span className="mtm-text-green-500">
                  {"Log your activities"}
                </span>
              </li>
              <li>
                <span className="mtm-text-green-500">
                  {"Track activities logged in the past"}
                </span>
              </li>
              <li>
                <span className="mtm-text-green-500">
                  {"Decide which activities you should cut-out on"}
                </span>
              </li>
              <li>
                <span className="mtm-text-green-500">
                  {"Start investing your time the right way"}
                </span>
              </li>
            </ul>
            <br />
            <SingleMeme
              twClassesImg={
                "mtm-border-[0.2px] mtm-px-0 mtm-min-w-full mtm-max-h-[300px]"
              }
              memeSrc={dwight}
            />
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}

export default NextSteps;
