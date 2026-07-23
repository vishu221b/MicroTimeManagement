import React, { useEffect, useRef, useState } from "react";
import { FiUser, FiLock, FiStar, FiCheck, FiCamera } from "react-icons/fi";
import {
  cancelSubscription,
  changeUserPassword,
  getBilling,
  getUserProfile,
  startCheckout,
  updateAvatar,
  updateUserDetails,
} from "../service/ApiService";
import { useConfirm } from "../components/ConfirmProvider";

const blankProfile = () => ({
  uid: "",
  username: "",
  email: "",
  firstName: "",
  lastName: "",
  dateOfBirth: "",
  avatarBase64: "",
});

const errorMessageFrom = (errorPayload) => {
  if (!errorPayload) return "Something went wrong.";
  if (errorPayload.error && errorPayload.error.message)
    return errorPayload.error.message;
  if (errorPayload.message) return errorPayload.message;
  return "Something went wrong.";
};

function Profile({ setToastState }) {
  const [profile, setProfile] = useState(blankProfile());
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [billing, setBilling] = useState(null);
  const [billingBusy, setBillingBusy] = useState(false);
  const confirm = useConfirm();

  const showSuccess = (message) =>
    setToastState({
      display: true,
      variant: "success",
      messages: [message],
      includePrefix: false,
      includeSuffix: false,
      suffix: "",
    });

  const showError = (messages) =>
    setToastState({
      display: true,
      variant: "error",
      messages: Array.isArray(messages) ? messages : [messages],
      includePrefix: true,
      includeSuffix: false,
      suffix: "",
    });

  useEffect(() => {
    getUserProfile((data, err) => {
      setLoading(false);
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      // /user/profile returns GenericMessageResponseDTO → { payload, message }.
      const payload = (data && data.payload) || data;
      if (payload) {
        setProfile({
          uid: payload.uid || "",
          username: payload.username || "",
          email: payload.email || "",
          firstName: payload.firstName || "",
          lastName: payload.lastName || "",
          dateOfBirth: payload.dateOfBirth || "",
          avatarBase64: payload.avatarBase64 || "",
        });
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleProfileSubmit = (e) => {
    e.preventDefault();
    if (
      !profile.uid ||
      !profile.username ||
      !profile.email ||
      !profile.firstName ||
      !profile.dateOfBirth
    ) {
      showError("Username, email, first name, and date of birth are required.");
      return;
    }
    setSavingProfile(true);
    updateUserDetails(profile, (data, err) => {
      setSavingProfile(false);
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      showSuccess("Profile updated.");
    });
  };

  const handlePasswordSubmit = (e) => {
    e.preventDefault();
    if (
      !passwordForm.oldPassword ||
      !passwordForm.newPassword ||
      !passwordForm.confirmPassword
    ) {
      showError("Please fill all password fields.");
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      showError("New password and confirmation do not match.");
      return;
    }
    if (passwordForm.newPassword.length < 8) {
      showError("New password must be at least 8 characters long.");
      return;
    }
    setSavingPassword(true);
    changeUserPassword(
      {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      },
      (data, err) => {
        setSavingPassword(false);
        if (err) {
          showError(errorMessageFrom(err));
          return;
        }
        setPasswordForm({
          oldPassword: "",
          newPassword: "",
          confirmPassword: "",
        });
        showSuccess("Password updated.");
      }
    );
  };

  useEffect(() => {
    getBilling((data, err) => {
      if (!err) setBilling(data);
    });
  }, []);

  const handleUpgrade = () => {
    setBillingBusy(true);
    startCheckout((data, err) => {
      setBillingBusy(false);
      if (err) return showError(errorMessageFrom(err));
      setBilling(data);
      if (data && data.checkoutUrl) {
        window.location = data.checkoutUrl;
        return;
      }
      showSuccess((data && data.message) || "Plan updated.");
    });
  };

  const handleCancelPlan = async () => {
    const ok = await confirm({
      title: "Cancel Pro?",
      message: "You'll return to the Free plan.",
      confirmLabel: "Cancel plan",
    });
    if (!ok) return;
    setBillingBusy(true);
    cancelSubscription((data, err) => {
      setBillingBusy(false);
      if (err) return showError(errorMessageFrom(err));
      setBilling(data);
      showSuccess((data && data.message) || "Cancelled.");
    });
  };

  const avatarInputRef = useRef(null);

  const onPickAvatar = (e) => {
    const file = e.target.files && e.target.files[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) {
      showError("Image must be at most 5 MB.");
      e.target.value = "";
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result;
      updateAvatar({ avatarBase64: dataUrl }, (data, err) => {
        if (err) return showError(errorMessageFrom(err));
        setProfile((p) => ({ ...p, avatarBase64: dataUrl }));
        showSuccess("Profile picture updated.");
      });
    };
    reader.readAsDataURL(file);
  };

  const initials = `${(profile.firstName || profile.username || "?")[0] || ""}${
    (profile.lastName || "")[0] || ""
  }`.toUpperCase();

  if (loading) {
    return (
      <div className="ui-page mtm-text-center ui-muted mtm-py-20">
        Loading profile…
      </div>
    );
  }

  const profileFields = [
    { key: "username", label: "Username", type: "text" },
    { key: "email", label: "Email", type: "email" },
    { key: "firstName", label: "First name", type: "text" },
    { key: "lastName", label: "Last name", type: "text" },
  ];

  return (
    <div className="ui-page ui-fade-in mtm-max-w-3xl">
      <div className="mtm-flex mtm-items-center mtm-gap-4 mtm-mb-8">
        <div className="mtm-relative mtm-shrink-0">
          <span className="mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-16 mtm-w-16 mtm-rounded-2xl mtm-overflow-hidden mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-text-white mtm-font-comic mtm-text-2xl mtm-border-[3px] mtm-border-ink mtm-shadow-comic-sm">
            {profile.avatarBase64 ? (
              <img src={profile.avatarBase64} alt="avatar" className="mtm-h-full mtm-w-full mtm-object-cover" />
            ) : (
              initials || <FiUser />
            )}
          </span>
          <button
            type="button"
            onClick={() => avatarInputRef.current && avatarInputRef.current.click()}
            className="mtm-absolute -mtm-bottom-1.5 -mtm-right-1.5 mtm-h-8 mtm-w-8 mtm-rounded-full mtm-bg-highlight mtm-border-2 mtm-border-ink mtm-flex mtm-items-center mtm-justify-center mtm-text-ink mtm-shadow-comic-sm hover:mtm-scale-110 mtm-transition-transform"
            aria-label="Change photo"
            title="Change photo"
          >
            <FiCamera size={14} />
          </button>
          <input ref={avatarInputRef} type="file" accept="image/*" className="mtm-hidden" onChange={onPickAvatar} />
        </div>
        <div>
          <p className="ui-eyebrow">Account</p>
          <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1 mtm-mb-0">
            {profile.firstName
              ? `${profile.firstName} ${profile.lastName}`.trim()
              : "My profile"}
          </h1>
          <p className="ui-muted mtm-text-sm mtm-mt-0.5 mtm-mb-0">
            @{profile.username}
          </p>
        </div>
      </div>

      <form onSubmit={handleProfileSubmit} className="ui-card mtm-p-6 mtm-mb-6">
        <div className="mtm-flex mtm-items-center mtm-gap-2 mtm-mb-5">
          <span className="ui-icon-tile mtm-h-9 mtm-w-9">
            <FiUser size={16} />
          </span>
          <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-m-0">
            Account details
          </h2>
        </div>
        <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-2">
          {profileFields.map((f) => (
            <div key={f.key}>
              <label className="ui-label" htmlFor={f.key}>
                {f.label}
              </label>
              <input
                id={f.key}
                type={f.type}
                value={profile[f.key]}
                onChange={(e) =>
                  setProfile((p) => ({ ...p, [f.key]: e.target.value }))
                }
                className="ui-input"
              />
            </div>
          ))}
          <div>
            <label className="ui-label" htmlFor="dateOfBirth">
              Date of birth
            </label>
            <input
              id="dateOfBirth"
              type="text"
              placeholder="DD-MM-YYYY"
              value={profile.dateOfBirth}
              onChange={(e) =>
                setProfile((p) => ({ ...p, dateOfBirth: e.target.value }))
              }
              className="ui-input"
            />
          </div>
        </div>
        <div className="mtm-mt-5">
          <button type="submit" className="ui-btn ui-btn-primary" disabled={savingProfile}>
            {savingProfile ? "Saving…" : "Save changes"}
          </button>
        </div>
      </form>

      <form onSubmit={handlePasswordSubmit} className="ui-card mtm-p-6">
        <div className="mtm-flex mtm-items-center mtm-gap-2 mtm-mb-5">
          <span className="ui-icon-tile mtm-h-9 mtm-w-9">
            <FiLock size={16} />
          </span>
          <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-m-0">
            Change password
          </h2>
        </div>
        <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-3">
          {[
            { key: "oldPassword", label: "Current password" },
            { key: "newPassword", label: "New password" },
            { key: "confirmPassword", label: "Confirm new password" },
          ].map((f) => (
            <div key={f.key}>
              <label className="ui-label" htmlFor={f.key}>
                {f.label}
              </label>
              <input
                id={f.key}
                type="password"
                value={passwordForm[f.key]}
                onChange={(e) =>
                  setPasswordForm((p) => ({ ...p, [f.key]: e.target.value }))
                }
                className="ui-input"
              />
            </div>
          ))}
        </div>
        <div className="mtm-mt-5">
          <button type="submit" className="ui-btn ui-btn-primary" disabled={savingPassword}>
            {savingPassword ? "Updating…" : "Update password"}
          </button>
        </div>
      </form>

      {billing && (
        <div className="ui-card mtm-p-6 mtm-mt-6">
          <div className="mtm-flex mtm-items-center mtm-gap-2 mtm-mb-5">
            <span className="ui-icon-tile mtm-h-9 mtm-w-9">
              <FiStar size={16} />
            </span>
            <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-m-0">Plan</h2>
          </div>
          <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-center sm:mtm-justify-between mtm-gap-4">
            <div>
              <div className="mtm-flex mtm-items-center mtm-gap-2">
                <span className="mtm-font-display mtm-font-bold mtm-text-xl mtm-text-content">
                  {billing.plan === "PRO" ? "Pro" : "Free"}
                </span>
                {billing.plan === "PRO" && (
                  <span className="ui-badge mtm-text-xs"><FiCheck size={12} /> active</span>
                )}
              </div>
              <p className="ui-muted mtm-text-sm mtm-mt-1 mtm-mb-0">
                {billing.paymentsConfigured
                  ? "Billing is configured."
                  : "Payments are stubbed in this build — upgrades are simulated. See the README to enable Stripe."}
              </p>
            </div>
            <div className="mtm-flex mtm-gap-2">
              {billing.plan === "PRO" ? (
                <button className="ui-btn ui-btn-ghost" disabled={billingBusy} onClick={handleCancelPlan}>
                  {billingBusy ? "Working…" : "Cancel plan"}
                </button>
              ) : (
                <button className="ui-btn ui-btn-primary" disabled={billingBusy} onClick={handleUpgrade}>
                  {billingBusy ? "Working…" : "Upgrade to Pro"}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Profile;
