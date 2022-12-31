import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";

function Top() {
  return (
    <div
      className="mtm-bg-no-repeat mtm-bg-cover mtm-bg-center mtm-font-mono"
      style={{
        backgroundImage: `url("https://bestanimations.com/media/clocks/1998493116funny-alarm-clock-animated-gif-3.gif")`,
      }}
    >
      <Container
        className="
      mtm-bg-black/80 
      mtm-mx-0 
      mtm-p-10 
      md:mtm-p-20 
      lg:mtm-pt-[4%] 
      mtm-min-h-screen/100 
      mtm-min-w-[100%]
    mtm-text-white/90 
      mtm-opacity-100"
      >
        <Container
          className="
        mtm-border-[0px] 
        mtm-rounded-md 
        mtm-border-white 
        mtm-p-10 
        mtm-shadow-2xl 
        mtm-shadow-cyan-700 
        mtm-mb-[10%]
        mtm-bg-black/20"
        >
          <div
            className="
          mtm-text-3xl lg:mtm-text-4xl 
          text-center mtm-mb-5 
          mtm-font-sans mtm-tracking-widest
          mtm-text-red-500
          "
          >
            <div className="mtm-underline mtm-font-sans mtm-text-3xl lg:mtm-text-5xl mtm-my-2 mtm-bg-gradient-to-r mtm-text-sky-300 mtm-py-0 mtm-tracking-widest mtm-animate-bounce">
              Micro time Management
            </div>
            <span className="mtm-tracking-wider mtm-text-2xl lg:mtm-text-3xl">
              What is it exactly?
            </span>
          </div>
          <hr />
          <Row className="mtm-my-5">
            <Col sm={12} lg={12}>
              <div
                className="
              mtm-bg-black/30 
              mtm-font-code
              mtm-tracking-widest 
              mtm-rounded-lg 
              mtm-mx-auto 
              mtm-text-lg 
              mtm-py-[15%] 
              sm:mtm-py-[10%] 
              lg:mtm-py-[5%] 
              mtm-text-justify 
              md:mtm-text-xl 
              lg:mtm-text-2xl 
              lg:mtm-text-center 
              mtm-px-[12%]"
              >
                Micro Time Management is an online tool aiming to make time
                management easier for you by allowing you to log in your daily
                activities time wise.
                <br />
                <br />
                It is, essentially, micro management of your time where you keep
                track of all your activities done in the past and review them
                over later to see your areas of improvement in terms of how you
                spend your time on which activities particularly throughtout the
                day.
              </div>
            </Col>
          </Row>
        </Container>
      </Container>
    </div>
  );
}

export default Top;
