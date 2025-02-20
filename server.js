import express from "express";
import multer from "multer";
import cors from "cors";
import {  execSync } from "child_process";
import fs from "fs";

const app = express();
const upload = multer({ dest: "uploads/" });

app.use(cors());
app.use(express.json());

app.post("/upload", upload.single("file"), (req, res) => {
    if (!req.file) {
      console.error("No file uploaded");
      return res.status(400).send("No file uploaded");
    }
  
    const inputFilePath = req.file.path;
    const outputFilePath = `uploads/${req.file.filename}.pptx`;
  
    console.log("Received file:", inputFilePath);
    console.log("output file path",outputFilePath);

    try {
        console.log("Generating PPTX...");
        execSync(`marp ${inputFilePath} --pptx -o ${outputFilePath}`, { stdio: "inherit" });
      
        console.log("PPTX generated:", outputFilePath);
        res.download(outputFilePath, "presentation.pptx", () => {
          fs.unlinkSync(inputFilePath);
          fs.unlinkSync(outputFilePath);
        });
      } catch (error) {
        console.error("Marp CLI Error:", error);
        res.status(500).send("Error generating PPTX");
      }
      
  
   
    });
 
  

app.listen(5000, () => console.log("Server running on port 5000"));
