import React from "react";
import { Container } from "react-bootstrap";

function Footer() {
  return (
    <div className="mtm-bg-black/90">
      <Container className="mtm-min-h-[15s0px] mtm-text-white/80 mtm-py-10">
        {/* <div>1</div>
        <div>2</div> */}
        <div className=" mtm-text-center mtm-align-text-bottom">
          Copyright &copy; 2022 Micro time management
        </div>
      </Container>
    </div>
  );
}

export default Footer;
