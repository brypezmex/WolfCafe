package edu.ncsu.csc326.wolfcafe.dto;





public class IngredientDto {
    
    
    private Long id;
    
    private String ingredientName;
    
    private Integer amount;
    
   
    public IngredientDto() {
        
    }

    public IngredientDto (  String ingredientName, Integer amount ) {
        this.ingredientName = ingredientName;
        this.amount = amount;
    }

    public Long getId () {
        return id;
    }

    public void setId ( Long id ) {
        this.id = id;
    }

    public String getIngredientName () {
        return ingredientName;
    }

    public void setIngredientName ( String ingredientName ) {
        this.ingredientName = ingredientName;
    }

    public Integer getAmount () {
        return amount;
    }

    public void setAmount ( Integer amount ) {
        this.amount = amount;
    }
    
    
    

}
