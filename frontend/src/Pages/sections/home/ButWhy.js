import React from "react";
import { Carousel } from "react-bootstrap";
import Container from "react-bootstrap/Container";
import Memes from "../../../components/Memes";
import { BsArrowLeftSquareFill, BsArrowRightSquareFill } from "react-icons/bs";

const memeStack = [
  {
    text: "First of all, let's go over some obvious facts...",
    gif: "https://media.tenor.com/OZPoWWCdJ8oAAAAC/michael-scott-wink.gif",
    alt: "Face the facts",
    highlightText: "facts",
    highlightColor: "green",
  },
  {
    text: "We all know how easy time management really is,",
    gif: "https://y.yarn.co/89da9842-a0c1-4d55-abda-1abb2990340b_text.gif",
    alt: "This is not easy",
    highlightText: "easy",
    highlightColor: "red",
  },
  {
    text: "I mean .. it certainly is no magic!!",
    gif: "https://thumbs.gfycat.com/FairCostlyAlabamamapturtle-size_restricted.gif",
    alt: "No magic...",
    highlightText: "no magic",
    highlightColor: "red",
  },
  {
    text: "The biggest problem with time management is consistency",
    gif: "https://images.ctfassets.net/i8q4h01orwwf/3bl0gCI0xcE0wTIWqcowDQ/aa4094f50037c90edad26b3eebc10143/Its_true.gif",
    alt: "It's true",
    highlightText: "consistency",
    highlightColor: "red",
  },
  {
    text: "If you have to attend to different activities throughout the day with no daily repetitive routine in particular, you need a way to track where you have been investing your time",
    gif: "https://y.yarn.co/d61db0a4-2a9a-4664-a3dd-a0f6d277905c_text.gif",
    alt: "Face the facts, duck",
    highlightText: "track where you have been investing your time",
    highlightColor: "red",
  },
  {
    text: "Here is where Micro Time Management comes into picture",
    gif: "https://media.tenor.com/_DJQCDxpU1cAAAAC/mighty-mouse-here-i-come.gif",
    alt: "Face the facts, duck",
    highlightText: "Micro Time Management",
    highlightColor: "green",
  },
  {
    text: "It gives you the power to track your activities and enables you to see how much time you've spent doing what, So that you can later on decide which activities you want to spend time on and hence manage your time well",
    gif: "https://i.gifer.com/origin/5b/5b4f8678d1866a5edb582c8452112fce.gif",
    alt: "Face the facts, duck",
    highlightText: "power",
    highlightColor: "yellow",
  },
];
const colorScheme = {
  red: "mtm-text-red-500",
  green: "mtm-text-green-500",
  yellow: "mtm-text-yellow-500",
  sky: "mtm-text-sky-500",
  orange: "mtm-text-orange-500",
};

const highlightArray = memeStack.map((e) => {
  if (e.highlightText) {
    return e.text.split(e.highlightText);
  }
  return [];
});

function ButWhy() {
  return (
    <>
      <div
        className="mtm-bg-repeat-y mtm-bg-cover mtm-bg-center mtm-font-sans mtm-tracking-widest"
        style={{
          // backgroundImage: `url("https://media.tenor.com/i4ibGB4XDB8AAAAC/quintincan-clock.gif")`,
          backgroundImage: `url("https://i.pinimg.com/originals/04/00/f1/0400f1ae341070283f5441097ef96d39.gif")`,
          borderTop: "1px solid grey",
        }}
      >
        <Container
          className="mtm-bg-black/90 mtm-text-white mtm-opacity-100 mtm-min-w-full"
          style={{
            textShadow: "0px 0px 30px white",
          }}
        >
          <div className="text-center mtm-py-14 mtm-text-4xl lg:mtm-text-5xl mtm-font-sans mtm-text-yellow-500 mtm-tracking-wider">
            That is okay but umm...
            <br />
            <span className="mtm-text-red-500">Why do I really need it?</span>
          </div>
          <hr />
          <div className="mtm-text-xl lg:mtm-text-3xl mtm-pt-10 mtm-text-center mtm-font-sans">
            Follow the slides below to know about it...
          </div>
          <Carousel
            slide
            indicators={false}
            wrap={false}
            touch
            prevIcon={
              <div
                className="mtm-ml-[-18px] mtm-animate-bounce"
                style={{
                  boxShadow: "0px 0px 20px 5px white",
                  borderRadius: "30px",
                }}
              >
                <BsArrowLeftSquareFill color="white" size={35} />
              </div>
            }
            nextIcon={
              <div
                className="mtm-ml-[18px] mtm-animate-bounce"
                style={{
                  boxShadow: "0px 0px 20px 5px white",
                  borderRadius: "30px",
                }}
              >
                <BsArrowRightSquareFill color="white" size={35} />
              </div>
            }
            className="
            mtm-min-h-[500px] 
            mtm-max-h-[500px] 
            md:mtm-min-h-[550px] 
            md:mtm-max-h-[550px] 
            xl:mtm-min-h-[650px] 
            xl:mtm-max-h-[650px] 
            mtm-m-0 
            sm:mtm-m-2 
            sm:mtm-mb-0
            mtm-mb-0 
            mtm-border-[0px] 
            mtm-rounded-xl 
            mtm-max-w-[80%] 
            mtm-mx-auto 
            sm:mtm-mx-auto 
            mtm-overflow-auto
            mtm-z-1"
            keyboard
          >
            {memeStack.map((memeStuff, index) => (
              <Carousel.Item
                key={index}
                interval={130000}
                className="mtm-my-[0%] mtm-rounded-2xl mtm-min-w-full"
              >
                <img
                  src={memeStuff.gif}
                  alt={memeStuff.alt}
                  className="
                  mtm-min-h-[220px] 
                  mtm-max-h-[220px] 
                  mtm-min-w-[100%] 
                  mtm-max-w-[100%] 
                  md:mtm-min-h-[300px] 
                  md:mtm-max-h-[300px] 
                  xl:mtm-min-h-[400px] 
                  xl:mtm-max-h-[400px] 
                  sm:mtm-min-w-[40%]
                  mtm-mx-auto
                  mtm-mt-[13%]
                  sm:mtm-mt-[3%]
                  "
                />
                <div className="mtm-overlay-auto mtm-text-2xl sm:mtm-text-xl md:mtm-text-3xl mtm-text-center mtm-p-[7%] md:mtm-px-[20%] mtm-py-[5%] lg:mtm-py-[2%] mtm-min-h-[100px] mtm-max-h-[200px] mtm-overflow-scroll">
                  <span className="mtm-text-cyan-500">
                    {index > 0 ? `#${index + " - "}` : ""}
                  </span>
                  {highlightArray[index].length ? highlightArray[index][0] : ""}
                  <span className={`${colorScheme[memeStuff.highlightColor]}`}>
                    {highlightArray[index].length > 0
                      ? memeStuff.highlightText
                      : ""}
                  </span>
                  {highlightArray[index].length ? highlightArray[index][1] : ""}
                  {!highlightArray[index].length ? memeStuff.text : ""}
                </div>
              </Carousel.Item>
            ))}
          </Carousel>
          {/* {memeStack.map((memeStuff, index) => (
            <Memes
              key={index}
              titleText={memeStuff.text}
              gifSrc={memeStuff.gif}
              gifAlt={memeStuff.alt}
              highlightText={
                memeStuff.highlightText ? memeStuff.highlightText : ""
              }
              highlightColor={
                memeStuff.highlightColor ? memeStuff.highlightColor : ""
              }
            />
          ))} */}
        </Container>
      </div>
    </>
  );
}

export default ButWhy;
