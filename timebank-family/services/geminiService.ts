import { GoogleGenAI, Type } from "@google/genai";

// Initialize Gemini
// Note: In a real app, strict error handling for missing API keys is essential.
// Here we gracefully degrade if no key is found for demo purposes.
const apiKey = process.env.API_KEY || '';
let ai: GoogleGenAI | null = null;

if (apiKey) {
  ai = new GoogleGenAI({ apiKey });
}

export interface OCRResult {
  success: boolean;
  name?: string;
  idNumber?: string;
  birthYear?: number;
  message?: string;
}

/**
 * Simulates or performs OCR on an ID card image.
 * If Gemini API is active, it uses the vision model to extract data.
 * Otherwise, it returns mock data for the demo.
 */
export const extractIdCardInfo = async (base64Image: string): Promise<OCRResult> => {
  if (!ai) {
    console.warn("Gemini API Key not found. Using Mock OCR.");
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    // Deterministic mock based on random chance for demo variety, 
    // or just return a standard successful response.
    return {
      success: true,
      name: "张伟",
      idNumber: "510101195805201234",
      birthYear: 1958, // Senior
      message: "OCR 识别成功 (模拟)"
    };
  }

  try {
    const modelId = "gemini-2.5-flash"; // Good balance of speed/cost for OCR
    const prompt = `
      Analyze this Chinese Resident Identity Card (or similar ID). 
      Extract the 'Name' and 'Citizen ID Number'. 
      Return a JSON object with keys: 'name' (string) and 'idNumber' (string).
      If you cannot clearly read it, return null for values.
    `;

    const response = await ai.models.generateContent({
      model: modelId,
      contents: {
        parts: [
          { inlineData: { mimeType: "image/jpeg", data: base64Image } },
          { text: prompt }
        ]
      },
      config: {
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            name: { type: Type.STRING },
            idNumber: { type: Type.STRING }
          }
        }
      }
    });

    const text = response.text;
    if (!text) throw new Error("No response from Gemini");

    const data = JSON.parse(text);
    
    if (data.name && data.idNumber) {
        // Extract birth year from ID (Chinese ID: 7th to 10th digit)
        // Example: 510101199001011234 -> 1990
        const idNum = data.idNumber.replace(/\s/g, '');
        let birthYear = 0;
        if (idNum.length >= 10) {
            const yearStr = idNum.substring(6, 10);
            birthYear = parseInt(yearStr, 10);
        }

        return {
            success: true,
            name: data.name,
            idNumber: idNum,
            birthYear: birthYear > 1900 ? birthYear : undefined,
            message: "身份证识别成功"
        };
    } else {
        return { success: false, message: "无法清晰识别身份证信息。" };
    }

  } catch (error) {
    console.error("Gemini OCR Error:", error);
    return { success: false, message: "识别失败，请重试。" };
  }
};