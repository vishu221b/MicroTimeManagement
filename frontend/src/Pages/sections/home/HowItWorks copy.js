import React from "react";
import { Container } from "react-bootstrap";
import SingleMeme from "../../../components/SingleMeme";
import timesUp from "../../../Times Up Ticking Clock.gif";

function HowItWorks() {
  return (
    <div
      className="mtm-bg-no-repeat mtm-bg-cover mtm-bg-center"
      style={{
        backgroundImage:
          "url('https://media.tenor.com/i4ibGB4XDB8AAAAC/quintincan-clock.gif')",
        //   "url('https://i.pinimg.com/originals/04/00/f1/0400f1ae341070283f5441097ef96d39.gif')",
        borderTop: "1px solid grey",
      }}
    >
      <Container className="mtm-bg-black/70 text-white mtm-min-w-full mtm-min-h-screen">
        <div className="mtm-font-sans mtm-text-4xl md:mtm-text-5xl mtm-text-center mtm-p-6 mtm-pt-[7%] sm:mtm-pt-[5%] mtm-underline mtm-text-red-500">
          How does it all really work?
        </div>
        <hr />
        <div className="mtm-p-20 mtm-text-xl md:mtm-text-2xl mtm-font-lex mtm-text-center">
          Basically, you start by logging your daily activities, for the current
          date respectively, into the app. Later on, you review your logged
          activities and see how much time you've spent doing what.
          <br />
          <br />
          <br />
          <span className="mtm-text-yellow-500">Using the MTM app,&nbsp;</span>
          you can:
          <br />
          <br />
          <ul className="mtm-text-justify mtm-list-['->'] mtm-list-inside mtm-text-yellow-500">
            <li>
              <span className="mtm-text-green-500">
                {" Log new activities,"}
              </span>
            </li>
            <li>
              <span className="mtm-text-green-500">
                {" Track activities logged in the past, for a particular date,"}
              </span>
            </li>
            <li>
              <span className="mtm-text-green-500">
                {
                  " Decide on which activities you should cut-out on and start investing time in which ones..."
                }
              </span>
            </li>
          </ul>
          <br />
          Now, it is often easier for us to know we need to set ourselves on the
          right track when someone points out the mistakes we make. But you
          cannot really have someone else observe you for the entire 24 hours to
          suggest you what to do with your time, well, except for your own self,
          ofcourse!!
          <br />
          <br />
          The way it works is that when you get into this micro habit of logging
          your activities regularly, it subconsciously starts affecting you in a
          positive manner and you start becoming disciplined slowly on your own!
          <br />
          <SingleMeme
            memeSrc={
              //   "https://www.icegif.com/wp-content/uploads/2022/05/icegif-156.gif"
              "https://media.tenor.com/p-DpG_Z5-WYAAAAC/tell-me-more-michael-scott.gif"
            }
          />
          <br />
          Just a single micro habit can, over the time, start making you feel
          responsible enough to make you start attending to other tasks
          throughout the day, which earlier would have been tough for you to do,
          beating procrastination slowly!!
          <br />
          <SingleMeme
            memeSrc={
              "https://y.yarn.co/9f13be54-366f-4b9f-b71b-e97f6d040cb2_text.gif"
            }
          />
          <br />
          Then, when you review your past activities and analyse it, you realize
          if you're really making the best use of your time and if not so, well
          ... you know it's time!
          <SingleMeme memeSrc={timesUp} />
        </div>
      </Container>
    </div>
  );
}

export default HowItWorks;
