import axios from "axios";

export default{
    addRecipe(recipe){
        return axios.post("/recipes", recipe);
    },
    listRecipes(){
        return axios.get("/recipes");
    }
}