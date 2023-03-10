package app;

import app.product.Product;
import app.product.ProductRepository;
import app.product.subproduct.BurgerSet;
import app.product.subproduct.Drink;
import app.product.subproduct.Hamburger;
import app.product.subproduct.Side;

import java.util.Scanner;

public class Cart {
    private Product[] items = new Product[0];
    private Scanner scanner = new Scanner(System.in);
    private ProductRepository productRepository;
    private Menu menu;
    public Cart(ProductRepository productRepository, Menu menu) {
        this.productRepository = productRepository;
        this.menu = menu;
    }

    public void printCart(){
        System.out.println("๐งบ ์ฅ๋ฐ๊ตฌ๋");
        System.out.println("-".repeat(60));

        //์ฌ๊ธฐ์ ์ฅ๋ฐ๊ตฌ๋ ์ํ๋ค์ ์ต์ ์ ๋ณด์ ํจ๊ป ์ถ๋ ฅ
        printCartItemDetails();

        System.out.println("-".repeat(60));
        System.out.printf("ํฉ๊ณ : %d์\n", calculateTotalPrice());

        System.out.println("์ด์ ์ผ๋ก ๋์๊ฐ๋ ค๋ฉด ์ํฐ๋ฅผ ๋๋ฅด์ธ์. ");
        scanner.nextLine();
    }
    void printCartItemDetails(){
        for(Product product : items){
            if(product instanceof BurgerSet) {
                BurgerSet burgerSet = (BurgerSet) product;
                System.out.printf(
                        "%s %6d์ (%s(์ผ์ฒฉ %d๊ฐ), %s(๋นจ๋ %s))\n",
                        burgerSet.getName(),
                        burgerSet.getPrice(),
                        burgerSet.getSide().getName(),
                        burgerSet.getSide().getKetchup(),
                        burgerSet.getDrink().getName(),
                        burgerSet.getDrink().hasStraw() ? "์์" : "์์"
                );
            } else if (product instanceof Hamburger) {
                Hamburger hamburger = (Hamburger) product;
                System.out.printf(
                        "%-8s %6d์ (๋จํ)\n",
                        hamburger.getName(),
                        hamburger.getPrice()
                );
            } else if (product instanceof Side) {
                Side side = (Side) product;
                System.out.printf(
                        "%-8s %6d์ (์ผ์ฒฉ %d๊ฐ)\n",
                        side.getName(),
                        side.getPrice(),
                        side.getKetchup()
                );
            } else if (product instanceof Drink) {
                Drink drink = (Drink) product;
                System.out.printf(
                        "%-8s %6d์ (๋นจ๋ %s)\n",
                        drink.getName(),
                        drink.getPrice(),
                        drink.hasStraw() ? "์์" : "์์"
                );
            }
        }
    }
    int calculateTotalPrice() {
        int totalPrice = 0;
        for (Product product : items) {
            totalPrice += product.getPrice();
        }
        return totalPrice;
    }

    public void addToCart(int productId){
        Product product = productRepository.findById(productId);

        chooseOption(product);

        if (product instanceof Hamburger) {
            Hamburger hamburger = (Hamburger) product;
            if(hamburger.isBurgerSet()) product = composeSet(hamburger);
        }
        Product newProduct;
        if(product instanceof Hamburger) newProduct = new Hamburger((Hamburger) product);
        else if(product instanceof Side) newProduct = new Side((Side) product);
        else if(product instanceof Drink) newProduct = new Drink((Drink) product);
        else newProduct = product; //BurgerSet๋ composeSet()์์ ์๋ก์ด BurgerSet์ ๋ง๋ค์ด ๋ฆฌํดํ๋ฏ๋ก ์๋ก์ด ์ธ์คํด์ค๋ฅผ ๋ง๋ค ํ์๊ฐ ์๋ค

        Product[] newItems = new Product[items.length+1];
        System.arraycopy(items,0,newItems,0,items.length);
        newItems[newItems.length-1] = newProduct;
        items = newItems;

        System.out.printf("[๐ฃ] %s ๋ฅผ(์) ์ฅ๋ฐ๊ตฌ๋์ ๋ด์์ต๋๋ค.\n",newProduct.getName());
    }
    private void chooseOption(Product product) {
        String input;

        if (product instanceof Hamburger) {
            Hamburger hamburger = (Hamburger) product;
            System.out.printf(
                    "๋จํ์ผ๋ก ์ฃผ๋ฌธํ์๊ฒ ์ด์? (1)_๋จํ(%d์) (2)_์ธํธ(%s์)\n",
                    hamburger.getPrice(),
                    hamburger.getBurgerSetPrice()
            );
            input = scanner.nextLine();
            if (input.equals("2")) hamburger.setIsBurgerSet(true);
        } else if (product instanceof Side) {
            Side side = (Side) product;
            System.out.println("์ผ์ฒฉ์ ๋ช๊ฐ๊ฐ ํ์ํ์ ๊ฐ์?");
            input = scanner.nextLine();
            side.setKetchup(Integer.parseInt(input));
        } else if (product instanceof Drink) {
            Drink drink = (Drink) product;
            System.out.println("๋นจ๋๊ฐ ํ์ํ์ ๊ฐ์? (1)_์ (2)_์๋์ค");
            input = scanner.nextLine();
            if (input.equals("2")) drink.setHasStraw(false);
        }
    }
    private BurgerSet composeSet(Hamburger hamburger) {
        System.out.println("์ฌ์ด๋๋ฅผ ๊ณจ๋ผ์ฃผ์ธ์");
        menu.printSides(false);

        String sideId = scanner.nextLine();
        Side side = (Side) productRepository.findById(Integer.parseInt(sideId));
        Side newSide = new Side(side);
        chooseOption(newSide);

        System.out.println("์๋ฃ๋ฅผ ๊ณจ๋ผ์ฃผ์ธ์.");
        menu.printDrinks(false);

        String drinkId = scanner.nextLine();
        Drink drink = (Drink) productRepository.findById(Integer.parseInt(drinkId));
        Drink newDrink = new Drink(drink);
        chooseOption(newDrink);

        String name = hamburger.getName() + "์ธํธ";
        int price = hamburger.getBurgerSetPrice();
        int kcal = hamburger.getKcal() + side.getKcal() + drink.getKcal();

        return new BurgerSet(name, price, kcal, hamburger, side, drink);
    }

}
