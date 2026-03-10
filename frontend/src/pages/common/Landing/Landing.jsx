import { Button } from "@/components/ui/button";
import {
  ShoppingCart,
  BarChart3,
  Users,
  Shield,
  FileText,
  Store,
  CheckCircle,
  Play,
  Star,
  ArrowDown,
} from "lucide-react";
import Header from "./Header";
import HeroSection from "./HeroSection";
import TrustedLogos from "./TrustedLogos";
import PricingCalculator from "./PricingCalculator";
// import FeatureComparisonSection from './FeatureComparison'
import LiveDemoSection from "./LiveDemoSection";
import FAQSection from "./FAQSection";
import ContactSection from "./ContactSection";
import Footer from "./Footer";
import WhyChooseUsSection from "./WhyChooseUsSection";
import KeyFeaturesSection from "./KeyFeaturesSection";

function Landing() {
  return (
    <div className="min-h-screen bg-background">
      {/* Header / Navbar */}
      <Header />

      {/* Hero Section */}
      <HeroSection />

      {/* Trusted Logos Section
      <TrustedLogos /> */}

      {/* Key Features Section */}
      <KeyFeaturesSection />

      {/* Why Choose Us Section */}
      <WhyChooseUsSection />

      {/* Live Demo Section
      <LiveDemoSection /> */}

      {/* FAQ Section */}
      <FAQSection />

      {/* Contact Section */}
      <ContactSection id="contact" />

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Landing;
