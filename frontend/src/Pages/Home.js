import React, { useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import anime from "animejs";
import {
  FiArrowRight,
  FiClock,
  FiBarChart2,
  FiFolder,
  FiBell,
  FiImage,
  FiZap,
  FiCheck,
  FiCalendar,
  FiLink2,
  FiSmartphone,
} from "react-icons/fi";
import useAuth from "../hooks/useAuth";

const Reveal = ({ children, delay = 0, className = "" }) => (
  <motion.div
    className={className}
    initial={{ opacity: 0, y: 26 }}
    whileInView={{ opacity: 1, y: 0 }}
    viewport={{ once: true, margin: "-60px" }}
    transition={{ duration: 0.5, delay, ease: "easeOut" }}
  >
    {children}
  </motion.div>
);

const FEATURES = [
  { icon: <FiClock />, title: "Log in a snap", text: "Name it, time it, done. Overlaps are auto-rejected and everything stays in order.", c: "mtm-from-primary mtm-to-accent" },
  { icon: <FiBarChart2 />, title: "See the story", text: "A live dashboard with a time-per-day chart and your top activities, at a glance.", c: "mtm-from-accent mtm-to-accent2" },
  { icon: <FiFolder />, title: "Projects & tasks", text: "Group work into projects with a To-do / Doing / Done board and nested sub-tasks.", c: "mtm-from-accent2 mtm-to-primary" },
  { icon: <FiBell />, title: "Never forget", text: "Schedule reminders with in-app + browser notifications (email optional).", c: "mtm-from-highlight mtm-to-accent" },
  { icon: <FiImage />, title: "Pin proof", text: "Attach files & images to any activity — receipts, screenshots, whatever you need.", c: "mtm-from-primary mtm-to-accent2" },
  { icon: <FiLink2 />, title: "Chain it up", text: "Link activities, tasks and projects into custom stories & routines.", c: "mtm-from-accent mtm-to-highlight" },
];

const STEPS = [
  { n: "1", title: "Sign up free", text: "No card, no setup. You're logging in seconds." },
  { n: "2", title: "Track your day", text: "Add activities, tasks and projects as you go." },
  { n: "3", title: "Level up", text: "Watch the charts reveal where your hours really go." },
];

const PLANS = [
  {
    name: "Free",
    price: "$0",
    period: "forever",
    tagline: "Everything to get your time under control.",
    cta: "Start free",
    to: "/register",
    highlight: false,
    features: [
      "Unlimited activities, tasks & projects",
      "Dashboard with charts",
      "In-app + browser reminders",
      "1 image per activity",
      "Add-to-calendar links",
    ],
  },
  {
    name: "Pro",
    price: "$4",
    period: "/ month",
    tagline: "For power trackers who want it all. Or $40/yr.",
    cta: "Go Pro",
    to: "/register",
    highlight: true,
    features: [
      "Everything in Free",
      "Email reminders",
      "Unlimited image attachments",
      "Advanced insights & exports",
      "Priority support + early features",
    ],
  },
];

const MARQUEE = ["Activities", "Projects", "Tasks", "Sub-tasks", "Reminders", "Charts", "Images", "Chaining", "Calendar", "PWA"];

function FloatingStickers() {
  const ref = useRef(null);
  useEffect(() => {
    if (!ref.current) return undefined;
    const targets = ref.current.querySelectorAll("[data-float]");
    const anim = anime({
      targets,
      translateY: [-10, 10],
      rotate: () => anime.random(-8, 8),
      direction: "alternate",
      loop: true,
      easing: "easeInOutSine",
      duration: () => anime.random(1800, 2800),
      delay: anime.stagger(200),
    });
    return () => anim.pause();
  }, []);
  const stickers = [
    { icon: <FiClock />, cls: "mtm-top-4 mtm-left-[8%] mtm-text-primary" },
    { icon: <FiBell />, cls: "mtm-top-10 mtm-right-[10%] mtm-text-accent" },
    { icon: <FiBarChart2 />, cls: "mtm-bottom-6 mtm-left-[14%] mtm-text-accent2" },
    { icon: <FiFolder />, cls: "mtm-bottom-10 mtm-right-[16%] mtm-text-highlight" },
  ];
  return (
    <div ref={ref} className="mtm-pointer-events-none mtm-absolute mtm-inset-0 mtm-hidden md:mtm-block">
      {stickers.map((s, i) => (
        <span key={i} data-float
          className={`mtm-absolute ${s.cls} mtm-inline-flex mtm-h-12 mtm-w-12 mtm-items-center mtm-justify-center mtm-rounded-2xl mtm-bg-surface mtm-border-[3px] mtm-border-ink mtm-shadow-comic mtm-text-xl`}>
          {s.icon}
        </span>
      ))}
    </div>
  );
}

function Home() {
  const { isAuthenticated } = useAuth();
  const primaryTo = isAuthenticated ? "/dashboard" : "/register";
  const primaryLabel = isAuthenticated ? "Open the app" : "Get started — free";

  return (
    <div className="mtm-overflow-hidden">
      {/* Hero */}
      <section className="mtm-relative mtm-max-w-6xl mtm-mx-auto mtm-px-6 mtm-pt-16 sm:mtm-pt-24 mtm-pb-14 mtm-text-center">
        {/* aurora */}
        <div className="mtm-pointer-events-none mtm-absolute mtm-inset-0 mtm-overflow-hidden">
          <div className="mtm-absolute mtm-top-[-10%] mtm-left-[10%] mtm-h-72 mtm-w-72 mtm-rounded-full mtm-bg-primary/30 mtm-blur-3xl mtm-animate-pulse" />
          <div className="mtm-absolute mtm-top-[10%] mtm-right-[8%] mtm-h-72 mtm-w-72 mtm-rounded-full mtm-bg-accent/30 mtm-blur-3xl mtm-animate-pulse" />
          <div className="mtm-absolute mtm-bottom-[-10%] mtm-left-[40%] mtm-h-72 mtm-w-72 mtm-rounded-full mtm-bg-accent2/25 mtm-blur-3xl mtm-animate-pulse" />
        </div>
        <FloatingStickers />

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }} className="mtm-relative">
          <span className="ui-badge mtm-mb-5"><FiZap size={13} /> Your time, but make it fun</span>
          <h1 className="mtm-font-comic mtm-text-content mtm-text-5xl sm:mtm-text-7xl mtm-leading-[0.95] mtm-max-w-3xl mtm-mx-auto">
            Track your time,{" "}
            <span className="ui-gradient-text">one activity at a time</span>
          </h1>
          <p className="ui-muted mtm-text-lg sm:mtm-text-xl mtm-mt-6 mtm-max-w-2xl mtm-mx-auto mtm-font-semibold">
            Micro Time Management turns your day into clear, colorful insight — activities, projects, tasks and reminders in one snappy app.
          </p>
          <div className="mtm-flex mtm-flex-wrap mtm-items-center mtm-justify-center mtm-gap-3 mtm-mt-9">
            <Link to={primaryTo} className="ui-btn ui-btn-primary mtm-text-base mtm-px-6 mtm-py-3">
              {primaryLabel} <FiArrowRight size={18} />
            </Link>
            <a href="#pricing" className="ui-btn ui-btn-ghost mtm-text-base mtm-px-6 mtm-py-3">See pricing</a>
          </div>
          <p className="ui-muted mtm-text-sm mtm-mt-4 mtm-font-semibold">No credit card · Free forever plan · Installable app</p>
        </motion.div>
      </section>

      {/* Marquee */}
      <div className="mtm-border-y-[3px] mtm-border-ink mtm-bg-surface mtm-py-3 mtm-overflow-hidden">
        <div className="mtm-flex mtm-gap-4 mtm-whitespace-nowrap mtm-animate-[marquee_22s_linear_infinite]">
          {[...MARQUEE, ...MARQUEE].map((m, i) => (
            <span key={i} className="mtm-font-comic mtm-text-2xl mtm-text-content/80 mtm-flex mtm-items-center mtm-gap-4">
              {m} <FiZap className="mtm-text-accent" />
            </span>
          ))}
        </div>
      </div>

      {/* Features */}
      <section id="features" className="mtm-max-w-6xl mtm-mx-auto mtm-px-6 mtm-py-20">
        <Reveal className="mtm-text-center mtm-mb-12">
          <p className="ui-eyebrow">Everything you need</p>
          <h2 className="mtm-font-comic mtm-text-content mtm-text-4xl sm:mtm-text-5xl mtm-mt-2">Simple to log. Powerful to review.</h2>
        </Reveal>
        <div className="mtm-grid mtm-grid-cols-1 sm:mtm-grid-cols-2 lg:mtm-grid-cols-3 mtm-gap-5">
          {FEATURES.map((f, i) => (
            <Reveal key={f.title} delay={i * 0.05}>
              <motion.div whileHover={{ y: -4, rotate: -0.6 }} className="ui-card mtm-p-6 mtm-h-full">
                <span className={`mtm-inline-flex mtm-h-12 mtm-w-12 mtm-items-center mtm-justify-center mtm-rounded-xl mtm-text-white mtm-text-xl mtm-border-[3px] mtm-border-ink mtm-shadow-comic-sm mtm-bg-gradient-to-br ${f.c}`}>
                  {f.icon}
                </span>
                <h3 className="mtm-font-comic mtm-text-2xl mtm-text-content mtm-mt-4">{f.title}</h3>
                <p className="ui-muted mtm-mt-1 mtm-font-medium">{f.text}</p>
              </motion.div>
            </Reveal>
          ))}
        </div>
      </section>

      {/* How it works */}
      <section id="how" className="mtm-max-w-6xl mtm-mx-auto mtm-px-6 mtm-pb-20">
        <Reveal className="mtm-text-center mtm-mb-12">
          <p className="ui-eyebrow">How it works</p>
          <h2 className="mtm-font-comic mtm-text-content mtm-text-4xl sm:mtm-text-5xl mtm-mt-2">Three steps to clarity</h2>
        </Reveal>
        <div className="mtm-grid mtm-grid-cols-1 md:mtm-grid-cols-3 mtm-gap-5">
          {STEPS.map((s, i) => (
            <Reveal key={s.n} delay={i * 0.08}>
              <div className="ui-card mtm-p-6 mtm-h-full">
                <div className="mtm-font-comic mtm-text-6xl ui-gradient-text">{s.n}</div>
                <h3 className="mtm-font-comic mtm-text-2xl mtm-text-content mtm-mt-2">{s.title}</h3>
                <p className="ui-muted mtm-mt-1 mtm-font-medium">{s.text}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </section>

      {/* Pricing */}
      <section id="pricing" className="mtm-max-w-5xl mtm-mx-auto mtm-px-6 mtm-pb-20">
        <Reveal className="mtm-text-center mtm-mb-12">
          <p className="ui-eyebrow">Pricing</p>
          <h2 className="mtm-font-comic mtm-text-content mtm-text-4xl sm:mtm-text-5xl mtm-mt-2">Cheap. Cheerful. Yours.</h2>
          <p className="ui-muted mtm-mt-2 mtm-font-semibold">Start free forever. Go Pro when you want the extras.</p>
        </Reveal>
        <div className="mtm-grid mtm-grid-cols-1 md:mtm-grid-cols-2 mtm-gap-6 mtm-items-start">
          {PLANS.map((p, i) => (
            <Reveal key={p.name} delay={i * 0.08}>
              <div className={`ui-card mtm-p-7 mtm-relative ${p.highlight ? "mtm-ring-4 mtm-ring-accent/40" : ""}`}>
                {p.highlight && (
                  <span className="ui-badge mtm-absolute -mtm-top-3 mtm-right-6">Most popular</span>
                )}
                <h3 className="mtm-font-comic mtm-text-3xl mtm-text-content">{p.name}</h3>
                <div className="mtm-flex mtm-items-end mtm-gap-1 mtm-mt-2">
                  <span className="mtm-font-comic mtm-text-5xl ui-gradient-text">{p.price}</span>
                  <span className="ui-muted mtm-font-bold mtm-mb-1">{p.period}</span>
                </div>
                <p className="ui-muted mtm-mt-1 mtm-font-medium">{p.tagline}</p>
                <ul className="mtm-mt-5 mtm-flex mtm-flex-col mtm-gap-2.5 mtm-list-none mtm-p-0">
                  {p.features.map((ft) => (
                    <li key={ft} className="mtm-flex mtm-items-start mtm-gap-2 mtm-text-content mtm-font-medium">
                      <span className="mtm-mt-0.5 mtm-inline-flex mtm-h-5 mtm-w-5 mtm-items-center mtm-justify-center mtm-rounded-full mtm-bg-ok mtm-text-white mtm-border-2 mtm-border-ink mtm-shrink-0">
                        <FiCheck size={12} />
                      </span>
                      {ft}
                    </li>
                  ))}
                </ul>
                <Link to={p.to} className={`ui-btn mtm-w-full mtm-mt-6 ${p.highlight ? "ui-btn-primary" : "ui-btn-ghost"}`}>
                  {p.cta} <FiArrowRight size={16} />
                </Link>
              </div>
            </Reveal>
          ))}
        </div>
        <p className="ui-muted mtm-text-center mtm-text-sm mtm-mt-6 mtm-font-semibold">
          <FiSmartphone className="mtm-inline mtm-mb-1" /> Install MTM as an app on desktop or mobile — it's a PWA.
        </p>
      </section>

      {/* Final CTA */}
      <section className="mtm-max-w-5xl mtm-mx-auto mtm-px-6 mtm-pb-24">
        <Reveal>
          <div className="ui-card mtm-p-10 mtm-text-center mtm-bg-gradient-to-br mtm-from-primary/10 mtm-to-accent/10">
            <FiCalendar className="mtm-mx-auto mtm-text-4xl mtm-text-accent mtm-mb-3" />
            <h2 className="mtm-font-comic mtm-text-content mtm-text-4xl sm:mtm-text-5xl">Ready to reclaim your hours?</h2>
            <p className="ui-muted mtm-mt-3 mtm-mb-7 mtm-font-semibold">Join now and see your first insights today.</p>
            <Link to={primaryTo} className="ui-btn ui-btn-primary mtm-text-base mtm-px-6 mtm-py-3">
              {primaryLabel} <FiArrowRight size={18} />
            </Link>
          </div>
        </Reveal>
      </section>
    </div>
  );
}

export default Home;
