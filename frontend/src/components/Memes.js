import React from "react";
import Container from "react-bootstrap/Container";

function Memes({ titleText, gifSrc, gifAlt, highlightText, highlightColor }) {
  let stringBefore;
  if (highlightText) {
    stringBefore = titleText.split(highlightText);
  }
  const colorScheme = {
    red: "mtm-text-red-500",
    green: "mtm-text-green-500",
    yellow: "mtm-text-yellow-500",
    sky: "mtm-text-sky-500",
    orange: "mtm-text-orange-500",
  };
  return (
    <>
      <hr className="mtm-animate-pulse" />
      <Container className="mtm-my-[2%] mtm-py-5">
        <div
          className="
        mtm-text-xl 
        mtm-font-bal 
        sm:mtm-text-2xl 
        lg:mtm-text-3xl 
        mtm-text-center
        mtm-mb-5 
        mtm-tracking-wider
        mtm-px-2
        mtm-mx-auto
        mtm-max-w-[90%]
        sm:mtm-max-w-[80%]
        md:mtm-max-w-[70%]
        "
        >
          {highlightText ? stringBefore[0] : ""}
          <span className={`${colorScheme[highlightColor]}`}>
            {highlightText}
          </span>
          {highlightText ? stringBefore[1] : ""}
          {highlightText ? "" : titleText}
        </div>
        <div className="mtm-p-0 mtm-mx-auto mtm-animate-none">
          <img
            className="
          mtm-rounded-xl 
          mtm-opacity-80 
          mx-auto 
          mtm-mx-auto 
          mtm-my-[10%]

          mtm-min-h-[150px] 
          mtm-max-h-[250px] 
          mtm-min-w-[50px] 
          mtm-max-w-[350px] 
          
          md:mtm-min-w-[50%] 
          md:mtm-max-w-[650px] 
          
          md:mtm-min-h-[350px] 
          md:mtm-max-h-[350px] 
          md:mtm-my-[7%]  
          
          lg:mtm-min-h-[400px] 
          lg:mtm-max-h-[400px] 
          lg:mtm-my-[4%]  
          "
            src={gifSrc}
            alt={gifAlt}
          />
        </div>
      </Container>
      <hr className="mtm-animate-pulse" />
    </>
  );
}

export default Memes;
