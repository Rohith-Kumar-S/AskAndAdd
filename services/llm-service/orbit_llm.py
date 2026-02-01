from fastapi import FastAPI
from langchain_ollama import ChatOllama
from langchain_core.prompts import PromptTemplate
import faiss
import pickle
import requests
from typing import List
import numpy as np

llm = ChatOllama(model="llama3.2")
review_extraction_prompt = PromptTemplate.from_template("""
You are a product category classifier. Given a user query, extract the most relevant category, subcategory, and brand name.

CATEGORIES:
- Beauty: Cosmetics and appearance products — makeup (eye, lip, face), skincare (serums, cleansers, moisturizers), hair color/dye, hair care (shampoo, conditioner), nail products (polish, press-ons), fragrances/cologne
- Personal Care: Daily hygiene and body maintenance — oral care (toothpaste, mouthwash), deodorant, body wash, soap, sunscreen, hand/body lotion for dry skin, shaving products, feminine hygiene, incontinence products
- Health and Medicine: Medical, wellness, and therapeutic products — over-the-counter medications (pain relief, allergy, cold), vitamins and supplements, first aid supplies, medical devices (thermometers, blood pressure monitors), orthopedic supports (insoles, braces), health-focused teas and remedies
- Clothing: Apparel and accessories worn on the body — shirts, pants, dresses, skirts, shoes, jackets, underwear, socks, hats, scarves, costumes, shoelaces, belts, swimwear, swimsuits, bikinis, activewear, athletic wear, loungewear
- Baby: Baby and toddler products — diapers, baby food, formula, baby clothing, strollers, car seats, cribs, toddler toys, baby skincare, nursing supplies
- Jewelry: Jewelry and watches — necklaces, rings, earrings, bracelets, watches, fine jewelry, fashion jewelry
- Home: Indoor furnishings and decor — bedding (sheets, blankets, pillows, duvet covers), curtains/drapes, rugs, throw pillows, furniture (beds, chairs, tables, headboards), home organization, storage
- Household Essentials: Consumable home supplies — cleaning products, laundry detergent, dish soap, paper towels, toilet paper, trash bags, light bulbs, batteries, air fresheners
- Home Improvement: Building, repair, and renovation — power tools, hand tools, hardware (screws, nails), wall/cabinet/furniture paint, painter's tape, plumbing supplies, electrical supplies, lumber, flooring, home repair materials
- Patio & Garden: Outdoor living and gardening — outdoor furniture, grills, planters, garden tools, plants, seeds, lawn care, patio decor, outdoor lighting
- Food: Groceries and consumables — fresh food, packaged foods, beverages, snacks, baking supplies (including decorative baking), condiments, frozen foods, pantry staples
- Electronics: Electronic devices and accessories — computers, laptops, phones, tablets, TVs, headphones, cameras, smart home devices, chargers, cables, small appliances
- Toys: Children's play items — action figures, dolls, board games, puzzles, building blocks, outdoor play equipment, educational toys, video games for kids
- Pets: Pet supplies and accessories — pet food (dog, cat, fish, bird), treats, pet toys, beds, collars, leashes, harnesses, grooming supplies, crates, cages, aquariums, litter and litter mats, pet gates, food bowls and mats, pet health products
- Sports & Outdoors: Athletic and outdoor recreation — sports equipment, fitness gear, camping supplies, hiking gear, bikes, athletic clothing, outdoor accessories, no shoes
- Arts Crafts & Sewing: Creative supplies — craft kits, fabric, yarn, sewing machines, paint supplies, scrapbooking, beads, knitting needles, DIY project materials
- Auto & Tires: Automotive products — tires, car parts, motor oil, car accessories, cleaning supplies for vehicles, car electronics, tools for auto repair
- Collectibles: Collectible and memorabilia items — trading cards, coins, stamps, figurines, vintage items, sports memorabilia, limited edition items

---

SUBCATEGORY EXTRACTION RULES:
- Example sub categories: Baby Itching & Rash Treatments, Clean Beauty Makeup, Cologne for Men, Baby Girls Clothing, Boys Outfit Sets, Girls Coats and Jackets,Mens Cargo Pants, Reebok Womens Socks, Womens Sweaters etc.
- The product only belongs to one sub category.
- Be specific in your answer.
- Must not go past 3 words for sub category.
- **If there is sex or gender, age, specific information, include it in the sub category**.

BRAND EXTRACTION RULES:
- Extract the brand name ONLY if explicitly mentioned in the query
- Look for proper nouns that represent brand/manufacturer names (e.g., Nike, Apple, L'Oreal, Pampers)
- Return only the brand name without additional words (e.g., "Nike" not "Nike brand")
- Common brand name patterns: capitalized words, well-known company names, designer names
- **If no brand is mentioned in the query, return "NaN"**

EDGE CASES:
- If the query could match multiple categories, choose the MOST SPECIFIC category
- If the query is ambiguous or unclear, use your best judgment based on the most likely intent

---

USER QUERY: {query}

OUTPUT FORMAT:
**CRITICAL: Respond with ONLY ONE line containing exactly three items, comma-separated.**
**Do NOT provide multiple classification options.**
**Do NOT include any explanations, alternatives, or additional text.**

Format: [Category Name], [Subcategory Name], [Brand Name]

If no brand can be identified, use "NaN" for the brand field.""")

app = FastAPI()

def load_faiss_index(path: str):
    """Load FAISS index and ID mapping from disk."""
    index = faiss.read_index(f"{path}.index")
    with open(f"{path}_id_mapping.pkl", "rb") as f:
        id_mapping = pickle.load(f)
    return index, id_mapping


def get_embedding(text: str, model: str = "nomic-embed-text:latest") -> List[float]:
    """Get embedding from Ollama API."""
    response = requests.post(
        "http://localhost:11434/api/embeddings",
        json={"model": model, "prompt": text}
    )
    response.raise_for_status()
    return response.json()["embedding"]

def search_similar(query: str, index: faiss.Index, id_mapping: dict, k: int = 5):
    """Search for similar products given a query."""
    query_embedding = np.array([get_embedding(query)], dtype=np.float32)
    distances, indices = index.search(query_embedding, k)
    
    results = {}
    for dist, idx in zip(distances[0], indices[0]):
        if idx != -1:
            results[id_mapping[idx]] = float(dist)
    return results


arts_index, arts_id_mapping = load_faiss_index("arts_crafts_&_sewing_index")
auto_tires_index, auto_tires_id_mapping = load_faiss_index("auto_&_tires_index")
baby_index, baby_id_mapping = load_faiss_index("baby_index")
beauty_index, beauty_id_mapping = load_faiss_index("beauty_index")
cloth_index, cloth_id_mapping = load_faiss_index("clothing_index")
collectibles_index, collectibles_id_mapping = load_faiss_index("collectibles_index")
electronics_index, electronics_id_mapping = load_faiss_index("electronics_index")
food_index, food_id_mapping = load_faiss_index("food_index")
health_index, health_id_mapping = load_faiss_index("health_and_medicine_index")
home_index, home_id_mapping = load_faiss_index("home_index")
home_improvement_index, home_improvement_id_mapping = load_faiss_index("home_improvement_index")
household_index, household_id_mapping = load_faiss_index("household_essentials_index")
jewelry_index, jewelry_id_mapping = load_faiss_index("jewelry_index")
patio_index, patio_id_mapping = load_faiss_index("patio_&_garden_index")
personal_care_index, personal_care_id_mapping = load_faiss_index("personal_care_index")
pets_index, pets_id_mapping = load_faiss_index("pets_index")
sports_index, sports_id_mapping = load_faiss_index("sports_&_outdoors_index")
toys_index, toys_id_mapping = load_faiss_index("toys_index")

index = {  "clothing_index" : (cloth_index, cloth_id_mapping), 
           "beauty_index" : (beauty_index, beauty_id_mapping), 
           "personal_care_index" : (personal_care_index, personal_care_id_mapping), 
           "health_and_medicine_index" : (health_index, health_id_mapping), 
           "baby_index" : (baby_index, baby_id_mapping), 
           "jewelry_index" : (jewelry_index, jewelry_id_mapping), 
           "home_index" : (home_index, home_id_mapping), 
           "household_essentials_index" : (household_index, household_id_mapping), 
           "home_improvement_index" : (home_improvement_index, home_improvement_id_mapping), 
           "patio_&_garden_index" : (patio_index, patio_id_mapping), 
           "food_index" : (food_index, food_id_mapping), 
           "electronics_index" : (electronics_index, electronics_id_mapping), 
           "toys_index" : (toys_index, toys_id_mapping), 
           "pets_index" : (pets_index, pets_id_mapping), 
           "sports_&_outdoors_index" : (sports_index, sports_id_mapping), 
           "arts_crafts_&_sewing_index" : (arts_index, arts_id_mapping), 
           "auto_&_tires_index" : (auto_tires_index, auto_tires_id_mapping), 
           "collectibles_index" : (collectibles_index, collectibles_id_mapping)
        }

@app.get("/extract")
def extract_category(query: str):
    prompt = review_extraction_prompt.format(query=query.lower())
    response = llm.invoke(prompt)
    response = response.content.strip().split(",")
    index, id_mapping = load_faiss_index("sub_category_index")
    print("Searching for sub category:", response, query.lower())
    results = search_similar(response[1], index, id_mapping, k=1)
    print("Sub category search results:", results)
    list_of_sub_cats = list(results.keys())
    if len(list_of_sub_cats) > 0:
        response[1] = list_of_sub_cats[0]
    print("Final extracted response:", response)
    return {
        "category": response[0].strip(),
        "sub_category": response[1].strip(),
        "brand": response[2].strip(),
        "vector_search_index": f"{'_'.join(response[0].lower().split())}_index"
    }
    
@app.get("/rank")
def rank_products(query: str, index_name: str):
    results = search_similar(query.lower(), index[index_name][0], index[index_name][1], k=30)
    return results