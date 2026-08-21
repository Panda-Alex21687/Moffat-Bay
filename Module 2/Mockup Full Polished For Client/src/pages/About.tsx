import { Link } from "react-router";

const MARINA_IMG = "https://images.unsplash.com/photo-1703728843356-515de7a3b134?w=800&h=520&fit=crop&auto=format";
const navy = "#061b3a";
const gold = "#eab53f";
const border = "#d9dde5";

function PageHero({ eyebrow, title, subtitle }: { eyebrow: string; title: string; subtitle: string }) {
  return (
    <section style={{
      background: navy,
      color: "#fff",
      padding: "60px 5%",
    }}>
      <div style={{ maxWidth: 1200, margin: "auto" }}>
        <div style={{ color: gold, fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 3, marginBottom: 12 }}>{eyebrow}</div>
        <h1 style={{ fontSize: "clamp(32px,4.5vw,52px)", lineHeight: 1.1, marginBottom: 16, maxWidth: 700 }}>{title}</h1>
        <div style={{ width: 70, height: 4, background: gold, marginBottom: 18 }} />
        <p style={{ color: "#c8d4e8", fontSize: 17, maxWidth: 600 }}>{subtitle}</p>
      </div>
    </section>
  );
}

export default function About() {
  return (
    <>
      <PageHero
        eyebrow="ABOUT MOFFAT BAY"
        title="A Marina Built Around the Boating Community"
        subtitle="Learn more about our long-term slip services, marina amenities, and the experience we want every visitor to have at Moffat Bay."
      />

      {/* Split section */}
      <section style={{ maxWidth: 1200, margin: "0 auto", padding: "70px 5%", display: "grid", gridTemplateColumns: "1fr 1fr", gap: 64, alignItems: "center" }}>
        <div>
          <div style={{ color: "#c8921e", fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 2, marginBottom: 10 }}>WELCOME ABOARD</div>
          <h2 style={{ color: navy, fontSize: "clamp(28px,3.5vw,40px)", lineHeight: 1.15, marginBottom: 14 }}>About Moffat Bay Marina</h2>
          <div style={{ width: 50, height: 3, background: gold, marginBottom: 20 }} />
          <p style={{ color: "#4a5568", marginBottom: 16 }}>
            Moffat Bay Marina is designed to give boaters a secure, convenient, and welcoming place to keep their vessels for long-term stays. The marina focuses on dependable slip access, useful amenities, and customer service that helps make time on the water easier.
          </p>
          <p style={{ color: "#4a5568" }}>
            Customers and visitors can use this website to learn about the marina before deciding whether they want to make a reservation. The Home, About Us, and Contact Us pages are public, so basic marina information can be viewed without logging in.
          </p>
        </div>
        <div style={{
          minHeight: 360,
          borderRadius: 10,
          background: `url(${MARINA_IMG}) center/cover no-repeat`,
          backgroundColor: "#8fa3c0",
          boxShadow: "0 10px 28px rgba(0,0,0,0.13)",
        }} role="img" aria-label="Boats and docks at Moffat Bay Marina" />
      </section>

      {/* Info cards band */}
      <section style={{ background: "#fff", borderTop: `1px solid ${border}`, borderBottom: `1px solid ${border}` }}>
        <div style={{ maxWidth: 1200, margin: "0 auto", padding: "56px 5%", display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 28 }}>
          {[
            { title: "Long-Term Slips", body: "The marina is centered around long-term slip reservations for customers who want a reliable place to keep their boat." },
            { title: "Marina Amenities", body: "Our public site highlights electric service, water access, maintained facilities, security, and other conveniences available to boaters." },
            { title: "Customer Support", body: "Visitors can use the Contact Us page to find the marina phone number, email, location, operating hours, and VHF channel." },
          ].map(({ title, body }) => (
            <article key={title} style={{
              background: "#fff",
              border: `1px solid ${border}`,
              borderRadius: 8,
              padding: "32px 28px",
            }}>
              <h3 style={{ color: navy, fontSize: 17, marginBottom: 10 }}>{title}</h3>
              <p style={{ color: "#4a5568", fontSize: 14, margin: 0 }}>{body}</p>
            </article>
          ))}
        </div>
      </section>

      {/* CTA section */}
      <section style={{ maxWidth: 1200, margin: "0 auto", padding: "70px 5%" }}>
        <div style={{ color: "#c8921e", fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 2, marginBottom: 10 }}>PUBLIC ACCESS</div>
        <h2 style={{ color: navy, fontSize: "clamp(26px,3.5vw,38px)", marginBottom: 14 }}>Explore Before You Sign In</h2>
        <div style={{ width: 50, height: 3, background: gold, marginBottom: 20 }} />
        <p style={{ color: "#4a5568", maxWidth: 540, marginBottom: 28 }}>
          The public information pages do not require an account. A visitor can move between the Home, About Us, and Contact Us pages directly from the navigation menu. Login is kept as a separate navigation option for features that may require an account in later user stories.
        </p>
        <Link to="/contact" style={{
          display: "inline-block",
          background: gold,
          color: navy,
          padding: "14px 28px",
          fontFamily: "Montserrat, Arial, sans-serif",
          fontWeight: 700,
          fontSize: 14,
          textDecoration: "none",
        }}>
          CONTACT THE MARINA
        </Link>
      </section>
    </>
  );
}
