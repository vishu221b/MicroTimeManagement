import React from "react";

function SingleMeme({
  memeSrc,
  memeCaption,
  memeAlt,
  style,
  twClassesDiv,
  twClassesImg,
}) {
  return (
    <div
      style={{ ...style }}
      className={`
        mtm-max-h-[300px]
        mtm-max-w-[100%]
        mtm-min-w-[100%]
        md:mtm-min-w-[450px]
        md:mtm-max-w-[450px]
        mtm-overflow-hidden
        mtm-mx-auto
        mtm-my-[12%]
        sm:mtm-my-[4%] ${twClassesDiv}`}
    >
      <img
        className={`mtm-mx-auto ${twClassesImg}`}
        src={memeSrc}
        alt={memeAlt}
      />
    </div>
  );
}

export default SingleMeme;
