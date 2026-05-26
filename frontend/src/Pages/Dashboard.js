import React from "react";
import { Link } from "react-router-dom";
import Button from "react-bootstrap/Button";

function Dashboard() {
  return (
    <div className="mtm-min-h-[60vh] mtm-bg-black mtm-text-white mtm-py-20 mtm-px-8 mtm-text-center">
      <h1 className="mtm-text-3xl mtm-tracking-wider mtm-text-sky-400">
        Welcome back
      </h1>
      <p className="mtm-mt-4 mtm-text-white/70">
        Jump into today's activity tracking.
      </p>
      <div className="mtm-mt-8">
        <Link to="/activity">
          <Button variant="warning" size="lg">
            <span className="mtm-tracking-widest">Track Activities</span>
          </Button>
        </Link>
      </div>
    </div>
  );
}

export default Dashboard;
