import React from "react";
import { Container, Form, FormCheck } from "react-bootstrap";

function Registration() {
  return (
    <div className="mtm-min-h-screen mtm-p-10">
      <Container className="">
        <div class="mtm-py-12 mtm-border-2">
          <h2 class="mtm-text-2xl mtm-font-bold">Solid</h2>
          <div class="mtm-mt-8 mtm-max-w-md mtm-mx-auto">
            <div class="mtm-grid mtm-grid-cols-1 mtm-gap-6">
              <label class="mtm-block">
                <span class="mtm-text-gray-700">Full name</span>
                <input
                  type="text"
                  class="
                    mtm-mt-1
                    mtm-block
                    mtm-w-full
                    mtm-rounded-md
                    mtm-bg-gray-100
                    mtm-border-transparent
                    focus:mtm-border-gray-500 focus:mtm-bg-white focus:mtm-ring-0
                  "
                  placeholder=""
                />
              </label>
              <label class="mtm-block">
                <span class="mtm-text-gray-700">Email address</span>
                <input
                  type="email"
                  class="
                    mtm-mt-1
                    mtm-block
                    mtm-w-full
                    mtm-rounded-md
                    mtm-bg-gray-100
                    mtm-border-transparent
                    focus:mtm-border-gray-500 focus:mtm-bg-white focus:mtm-ring-0
                  "
                  placeholder="john@example.com"
                />
              </label>
              <label class="mtm-block">
                <span class="mtm-text-gray-700">When is your event?</span>
                <input
                  type="date"
                  class="
                    mtm-mt-1
                    mtm-p-2
                    mtm-block
                    mtm-w-full
                    mtm-rounded-md
                    mtm-bg-gray-100
                    mtm-border-transparent
                    focus:mtm-border-gray-500 focus:mtm-bg-white focus:mtm-ring-0
                  "
                />
              </label>
              <label class="mtm-block">
                <span class="mtm-text-gray-700">What type of event is it?</span>
                <select
                  class="
                    mtm-block
                    mtm-w-full
                    mtm-mt-1
                    mtm-rounded-md
                    mtm-bg-gray-100
                    mtm-border-transparent
                    focus:mtm-border-gray-500 focus:mtm-bg-white focus:mtm-ring-0
                  "
                >
                  <option>Corporate event</option>
                  <option>Wedding</option>
                  <option>Birthday</option>
                  <option>Other</option>
                </select>
              </label>
              <label class="mtm-block">
                <span class="mtm-text-gray-700">Additional details</span>
                <textarea
                  class="
                    mtm-mt-1
                    mtm-block
                    mtm-w-full
                    mtm-rounded-md
                    mtm-bg-gray-100
                    mtm-border-transparent
                    focus:mtm-border-gray-500 focus:mtm-bg-white focus:mtm-ring-0
                  "
                  rows="3"
                ></textarea>
              </label>
              <div class="mtm-block">
                <div class="mtm-mt-2">
                  <div>
                    <label class="mtm-inline-flex mtm-items-center">
                      <input
                        type="mtm-checkbox"
                        class="
                          mtm-rounded
                          mtm-bg-gray-200
                          mtm-border-transparent
                          focus:mtm-border-transparent focus:mtm-bg-gray-200
                          mtm-text-gray-700
                          focus:mtm-ring-1 focus:mtm-ring-offset-2 focus:mtm-ring-gray-500
                        "
                      />
                      <span class="mtm-ml-2">
                        Email me news and special offers
                      </span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <Form>
          <Form.Label>Name</Form.Label>
        </Form>
      </Container>
    </div>
  );
}

export default Registration;
