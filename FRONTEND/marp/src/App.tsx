import { useState } from "react";
import "./App.css";

function App() {
  const [file, setFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false); // Loading state

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      setFile(event.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!file) {
      alert("Please select a file");
      return;
    }
    const fileExtension = file.name.split('.').pop();

    if (fileExtension !== 'md') {
      alert("Only .md files are accepted");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    setIsLoading(true); // Set loading state to true

    try {
      const response = await fetch("http://localhost:5000/upload", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "presentation.pptx";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      } else {
        alert("Failed to generate PPTX");
      }
    } catch (error) {
      console.error("Error during upload:", error);
      alert("An error occurred while uploading the file.");
    } finally {
      setIsLoading(false); // Reset loading state
    }
  };

  return (
    <div className="container">
      {/* Left Side: Image */}
      <div className="image-container">
        <img
          src="https://dev.portal.mvpin90days.com/assets/logo-light-ypugESCg.png"
          alt="Description"
          className="image"
        />
      </div>

      {/* Right Side: Upload Section */}
      <div className="upload-container">
        <h1 className="header">Create your Pitch Deck!</h1>
        <input
          type="file"
          accept=".md"
          onChange={handleFileChange}
          className="file-input"
        />
        <button
          onClick={handleUpload}
          className="upload-button"
          disabled={isLoading} // Disable button while loading
        >
          {isLoading ? "Generating..." : "Upload & Generate PPTX"}
        </button>
      </div>
    </div>
  );
}

export default App;