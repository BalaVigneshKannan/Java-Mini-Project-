import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;


public class SmartCartApp extends JFrame {

    
    static abstract class Product {
        private final String id;
        private final String name;
        private final double price;

        Product(String id, String name, double price) {
            this.id = id; this.name = name; this.price = price;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public abstract String getCategory();
        @Override public String toString() { return name + " — AED " + price; }
    }

    static class Electronics extends Product {
        Electronics(String id, String name, double price) { super(id, name, price); }
        @Override public String getCategory() { return "Electronics"; }
    }

    static class Clothing extends Product {
        Clothing(String id, String name, double price) { super(id, name, price); }
        @Override public String getCategory() { return "Clothing"; }
    }

    static class Cart {
        private final LinkedHashMap<String, Product> items = new LinkedHashMap<>();
        void add(Product p) { items.put(p.getId(), p); }
        void remove(String id) { items.remove(id); }
        Collection<Product> getAll() { return items.values(); }
        double total() {
            double t = 0; for (Product p : items.values()) t += p.getPrice(); return t;
        }
        boolean isEmpty() { return items.isEmpty(); }
        void clear() { items.clear(); }
        int size() { return items.size(); }
    }

    static class Reservation {
        Product product;
        LocalDate reservationDate;
        LocalDate plannedPurchaseDate;
        double fee;
        boolean cancelled = false;
        boolean purchased = false;
        LocalDate purchaseDate;

        Reservation(Product product, LocalDate reservationDate, LocalDate plannedPurchaseDate, double fee) {
            this.product = product;
            this.reservationDate = reservationDate;
            this.plannedPurchaseDate = plannedPurchaseDate;
            this.fee = fee;
        }
    }

  
    private final SmartCartManager manager = new SmartCartManager();

    
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    
    private final DecimalFormat money = new DecimalFormat("#0.00");

    
    private final Color brandBlue = new Color(28, 150, 210); 
    private final Color softBlue = new Color(241, 249, 253); 
    private final Color panelBg = new Color(250, 250, 255);
    
    
    private final Color buttonColor = new Color(30, 100, 150); 
    private final Color listStripeColor = new Color(245, 245, 245); 
    
    
    private final Color priceColor = new Color(16, 112, 32); 
    private final String priceColorHex = "#107020"; 
    
    
    private final Font heading = new Font("Arial", Font.BOLD, 22); 
    private final Font normal = new Font("Arial", Font.PLAIN, 15); 

    private static final double COD_FEE = 20.0; 

    
    private double userBudget = 0.0;
    private boolean budgetSet = false;
    private final JProgressBar budgetBar = new JProgressBar(0, 100);
    private final JLabel budgetLabel = new JLabel("Budget not set");

    
    private String currentUser = null;

    public SmartCartApp() {
        setTitle("SmartCart — Modern Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        
        root.setBackground(Color.WHITE);
        root.add(buildWelcomePanel(), "welcome");
        root.add(buildSignupPanel(), "signup");
        root.add(buildLoginPanel(), "login");
        root.add(buildHomePanel(), "home");
        root.add(buildElectronicsPanel(), "electronics");
        root.add(buildClothingPanel(), "clothing");
        root.add(buildCartPanel(), "cart");
        root.add(buildCheckoutPanel(), "checkout");
        root.add(buildReservationsPanel(), "reservations");

        add(root);
        cards.show(root, "welcome");
    }
    
    
    private String colorizePrices(String text, String priceColorHex) {
      
        String delimiter = " — ";
        String currency = "AED ";
        String fullPattern = delimiter + currency;

        
        if (text.contains(fullPattern)) {
           
            int sepIndex = text.indexOf(fullPattern);
            
           
            String namePart = text.substring(0, sepIndex + delimiter.length()); 
           
            String priceAndSuffix = text.substring(sepIndex + delimiter.length()); 
            
            
            int pipeIndex = priceAndSuffix.indexOf('|');
            
            if (pipeIndex == -1) {
                
                return "<html>" + namePart + "<font color=\"" + priceColorHex + "\">" + priceAndSuffix + "</font></html>";
            } else {
                
                String priceVal = priceAndSuffix.substring(0, pipeIndex); 
                
                String suffix = priceAndSuffix.substring(pipeIndex); 
                
                String middleColored = namePart + "<font color=\"" + priceColorHex + "\">" + priceVal + "</font>" + suffix;
                
                
                String feePattern = "Fee: " + currency;
                int feeIndex = middleColored.lastIndexOf(feePattern);
                if (feeIndex != -1) {
                    
                    String feePrefix = middleColored.substring(0, feeIndex + feePattern.length()); 
                    
                    String feeValue = middleColored.substring(feeIndex + feePattern.length()); 
                    
                   
                    String feeBlockStart = "Fee: ";
                    int newFeeIndex = middleColored.lastIndexOf(feeBlockStart);
                    if (newFeeIndex != -1) {
                        String prefixBeforeFee = middleColored.substring(0, newFeeIndex + feeBlockStart.length()); // "... | Fee: "
                        String feeValueWithCurrency = middleColored.substring(newFeeIndex + feeBlockStart.length()); // "AED 5.00"

                        return "<html>" + prefixBeforeFee + "<font color=\"" + priceColorHex + "\">" + feeValueWithCurrency + "</font></html>";
                    }
                }
                
                
                return "<html>" + middleColored + "</html>";
            }
        }
        
        
        return "<html>" + text + "</html>";
    }

    
    private class ZebraListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            
            setText(colorizePrices(value.toString(), priceColorHex));

            setFont(normal);
            
            
            if (index % 2 == 0) {
                setBackground(listStripeColor);
            } else {
                setBackground(list.getBackground());
            }
            
            
            if (isSelected) {
                setBackground(buttonColor.darker()); 
                setForeground(Color.WHITE);
                
            } else {
               
                setForeground(Color.BLACK); 
            }
            
            
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            
            return this;
        }
    }

   
    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        
        b.setBackground(buttonColor); 
        
        b.setForeground(Color.WHITE); 
        b.setFocusPainted(false);
        b.setFont(normal);
        
        b.setBorder(new EmptyBorder(10,20,10,20)); 
        
        b.setRolloverEnabled(false); 
        return b;
    }

    private JPanel centerWrap(JComponent comp) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(softBlue);
        p.add(comp);
        return p;
    }

    
    static class DigitFilter extends DocumentFilter {
        private final int maxLength;
        
        public DigitFilter() {
            this.maxLength = -1; 
        }
        
        public DigitFilter(int maxLength) {
            this.maxLength = maxLength;
        }
        
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            StringBuilder sb = new StringBuilder();
            for (char c : string.toCharArray()) if (Character.isDigit(c)) sb.append(c);
            
            String filtered = sb.toString();
            if (maxLength > 0) {
                int availableLength = maxLength - fb.getDocument().getLength();
                if (availableLength > 0) {
                    filtered = filtered.substring(0, Math.min(availableLength, filtered.length()));
                } else {
                    filtered = "";
                }
            }
            super.insertString(fb, offset, filtered, attr);
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) if (Character.isDigit(c)) sb.append(c);
            
            String filtered = sb.toString();
            if (maxLength > 0) {
                int currentLength = fb.getDocument().getLength();
                int availableLength = maxLength - (currentLength - length);
                if (availableLength > 0) {
                    filtered = filtered.substring(0, Math.min(availableLength, filtered.length()));
                } else {
                    filtered = "";
                }
            }
            super.replace(fb, offset, length, filtered, attrs);
        }
    }

    static class ExpiryFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            StringBuilder sb = new StringBuilder();
            for (char c : string.toCharArray()) if (Character.isDigit(c) || c == '/') sb.append(c);
            
            
            String filtered = sb.toString();
            if (fb.getDocument().getLength() + filtered.length() > 5) {
                filtered = filtered.substring(0, 5 - fb.getDocument().getLength());
            }
            super.insertString(fb, offset, filtered, attr);
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) if (Character.isDigit(c) || c == '/') sb.append(c);
            
            
            String filtered = sb.toString();
            int newLength = fb.getDocument().getLength() - length + filtered.length();
            if (newLength > 5) {
                filtered = filtered.substring(0, 5 - (fb.getDocument().getLength() - length));
            }
            super.replace(fb, offset, length, filtered, attrs);
        }
    }

    private void applyDigitFilter(JTextField tf) {
        Document d = tf.getDocument();
        if (d instanceof AbstractDocument) ((AbstractDocument)d).setDocumentFilter(new DigitFilter());
    }

    private void applyDigitFilter(JTextField tf, int maxLength) {
        Document d = tf.getDocument();
        if (d instanceof AbstractDocument) ((AbstractDocument)d).setDocumentFilter(new DigitFilter(maxLength));
    }

    private void applyExpiryFilter(JTextField tf) {
        Document d = tf.getDocument();
        if (d instanceof AbstractDocument) ((AbstractDocument)d).setDocumentFilter(new ExpiryFilter());
    }

    
    private JPanel buildWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(40,40,40,40));

        JLabel title = new JLabel("Welcome to SmartCart");
        title.setFont(heading);
        title.setForeground(brandBlue);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Modern demo: Shop electronics and clothing — Reservations added");
        subtitle.setFont(normal);
        subtitle.setForeground(Color.DARK_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(title);
        inner.add(Box.createRigidArea(new Dimension(0,8)));
        inner.add(subtitle);
        inner.add(Box.createRigidArea(new Dimension(0,24)));

        JPanel btnRow = new JPanel();
        btnRow.setOpaque(false);
        JButton signup = makeButton("Sign Up");
        JButton login = makeButton("Log In");
        signup.addActionListener(e -> cards.show(root, "signup"));
        login.addActionListener(e -> cards.show(root, "login"));
        btnRow.add(signup);
        btnRow.add(Box.createRigidArea(new Dimension(8,0)));
        btnRow.add(login);

        inner.add(btnRow);
        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    
    private JPanel buildSignupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(new EmptyBorder(24,24,24,24),
                                          new LineBorder(Color.LIGHT_GRAY, 1, true)));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(350, 380));


        JLabel h = new JLabel("Create an account");
        h.setFont(heading); h.setForeground(brandBlue); h.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(h);
        card.add(Box.createRigidArea(new Dimension(0,12)));

        JTextField userF = new JTextField(); userF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        JPasswordField passF = new JPasswordField();
                passF.setDocument(new javax.swing.text.PlainDocument() {
                    @Override
                    public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                        if (str == null) return;
                        if ((getLength() + str.length()) <= 8) {
                            super.insertString(offs, str, a);
                        }
                    }
                }); passF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        userF.setFont(normal); passF.setFont(normal);

        JLabel hint = new JLabel("<html><small>Username: letters/digits/underscore, start with letter, 3-12 chars.</small></html>");
        hint.setFont(new Font("Arial", Font.ITALIC, 11)); 
        hint.setForeground(Color.DARK_GRAY);

        card.add(new JLabel("Username:")); card.add(userF); card.add(hint); card.add(Box.createRigidArea(new Dimension(0,8)));
        card.add(new JLabel("Password (max 8 chars):")); card.add(passF); card.add(Box.createRigidArea(new Dimension(0,12)));

        JButton register = makeButton("Register");
        register.addActionListener(e -> {
            String u = userF.getText().trim();
            String p = new String(passF.getPassword()).trim();

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill both fields.");
                return;
            }

            
            if (!u.matches("^[A-Za-z][A-Za-z0-9_]{2,11}$")) {
                JOptionPane.showMessageDialog(this, "Username invalid. Must start with a letter, 3-12 chars, letters/digits/underscore.");
                return;
            }

            if (manager.getUsernames().contains(u)) {
                JOptionPane.showMessageDialog(this, "Username exists.");
                return;
            }
            manager.getUsernames().add(u); manager.getPasswords().add(p);
            JOptionPane.showMessageDialog(this, "Signup successful — please log in.");
            userF.setText(""); passF.setText("");
            cards.show(root, "login");
        });

        JButton back = new JButton("Back"); back.setFont(normal);
        
        back.setBackground(buttonColor);
        back.setForeground(Color.WHITE);
        back.setRolloverEnabled(false);
        back.addActionListener(e -> cards.show(root, "welcome"));

        JPanel btnRow = new JPanel(); btnRow.setOpaque(false);
        btnRow.add(register); btnRow.add(Box.createRigidArea(new Dimension(8,0))); btnRow.add(back);
        card.add(btnRow);

        panel.add(centerWrap(card), BorderLayout.CENTER);
        return panel;
    }

    
    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(new EmptyBorder(24,24,24,24),
                                          new LineBorder(Color.LIGHT_GRAY, 1, true)));
        card.setBackground(Color.WHITE);

        JLabel h = new JLabel("Log in");
        h.setFont(heading); h.setForeground(brandBlue); card.add(h);
        card.add(Box.createRigidArea(new Dimension(0,12)));

        JTextField userF = new JTextField();
        userF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JPasswordField passF = new JPasswordField();
                passF.setDocument(new javax.swing.text.PlainDocument() {
                    @Override
                    public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                        if (str == null) return;
                        if ((getLength() + str.length()) <= 8) {
                            super.insertString(offs, str, a);
                        }
                    }
                });
        passF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        card.add(new JLabel("Username:")); card.add(userF); card.add(Box.createRigidArea(new Dimension(0,8)));
        card.add(new JLabel("Password:")); card.add(passF); card.add(Box.createRigidArea(new Dimension(0,12)));

        JButton login = makeButton("Login");
        login.addActionListener(e -> {
            String u = userF.getText().trim();
            String p = new String(passF.getPassword()).trim();
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this,"Fill both fields.");
                return;
            }
            if (manager.getUsernames().contains(u)) {
                int idx = manager.getUsernames().indexOf(u);
                if (manager.getPasswords().get(idx).equals(p)) {
                    userF.setText(""); passF.setText("");
                    currentUser = u;
                    
                    askForBudget();
                    cards.show(root, "home");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        });

        JButton back = new JButton("Back"); back.setFont(normal);
        
        back.setBackground(buttonColor);
        back.setForeground(Color.WHITE);
        back.setRolloverEnabled(false);
        back.addActionListener(e -> cards.show(root, "welcome"));
        JPanel btnRow = new JPanel(); btnRow.setOpaque(false);
        btnRow.add(login); btnRow.add(Box.createRigidArea(new Dimension(8,0))); btnRow.add(back);
        card.add(btnRow);

        panel.add(centerWrap(card), BorderLayout.CENTER);
        return panel;
    }

    
    private void askForBudget() {
        String s = JOptionPane.showInputDialog(this,
                "Enter your budget limit (AED):", "Set Budget", JOptionPane.PLAIN_MESSAGE);
        if (s == null) {
            budgetSet = false;
            userBudget = 0.0;
            budgetLabel.setText("Budget not set");
            budgetBar.setValue(0);
            return;
        }
        try {
            double b = Double.parseDouble(s.trim());
            if (b <= 0) {
                JOptionPane.showMessageDialog(this, "Budget must be a positive number.");
                budgetSet = false;
                userBudget = 0.0;
                budgetLabel.setText("Budget not set");
                budgetBar.setValue(0);
                return;
            }
            userBudget = b;
            budgetSet = true;
            updateBudgetUI();
            JOptionPane.showMessageDialog(this, "Budget set to AED " + money.format(userBudget));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number. Budget not set.");
            budgetSet = false;
            userBudget = 0.0;
            budgetLabel.setText("Budget not set");
            budgetBar.setValue(0);
        }
    }

    private void updateBudgetUI() {
        if (!budgetSet || userBudget <= 0) {
            budgetLabel.setText("Budget not set");
            budgetBar.setValue(0);
            return;
        }
        double total = manager.getCart().total();
        double percent = Math.min(100.0, (total / userBudget) * 100.0);
        budgetBar.setValue((int) Math.round(percent));
        budgetBar.setStringPainted(true);
        budgetBar.setString((int)Math.round(percent) + "% used (AED " + money.format(total) + " / AED " + money.format(userBudget) + ")");
        budgetLabel.setText("Budget: AED " + money.format(userBudget) + " | In cart: AED " + money.format(total));
    }

    
    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);

        JLabel title = new JLabel("Shop Categories");
        title.setFont(heading); title.setForeground(brandBlue);
        title.setBorder(new EmptyBorder(16,16,8,16));

        
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        budgetBar.setPreferredSize(new Dimension(400, 26));
        budgetBar.setValue(0);
        budgetBar.setForeground(brandBlue);
        budgetLabel.setFont(normal);
        topRow.add(budgetLabel, BorderLayout.WEST);
        topRow.add(budgetBar, BorderLayout.EAST);
        topRow.setBorder(new EmptyBorder(8,12,8,12));

        JPanel grid = new JPanel(new GridLayout(3,2,16,16)); 
        grid.setBorder(new EmptyBorder(24,24,24,24));
        grid.setOpaque(false);

        JButton electronicsBtn = makeButton("Electronics");
        JButton clothingBtn = makeButton("Clothing");
        JButton viewCartBtn = makeButton("View Cart");
        JButton checkoutBtn = makeButton("Checkout");
        
        JButton logoutBtn = new JButton("Log out"); logoutBtn.setFont(normal);
        
        logoutBtn.setBackground(buttonColor);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setRolloverEnabled(false);
        
        JButton setBudgetBtn = new JButton("Set/Change Budget"); setBudgetBtn.setFont(normal);
        
        setBudgetBtn.setBackground(buttonColor);
        setBudgetBtn.setForeground(Color.WHITE);
        setBudgetBtn.setRolloverEnabled(false);
        
        JButton reservationsBtn = makeButton("Reservations");

        electronicsBtn.addActionListener(e -> cards.show(root, "electronics"));
        clothingBtn.addActionListener(e -> cards.show(root, "clothing"));
        viewCartBtn.addActionListener(e -> cards.show(root, "cart"));
        checkoutBtn.addActionListener(e -> cards.show(root, "checkout"));
        reservationsBtn.addActionListener(e -> cards.show(root, "reservations"));
        logoutBtn.addActionListener(e -> {
           
            manager.getCart().clear();
            budgetSet = false; userBudget = 0.0;
            currentUser = null;
            updateBudgetUI();
            cards.show(root, "welcome");
        });
        setBudgetBtn.addActionListener(e -> askForBudget());

        grid.add(electronicsBtn);
        grid.add(clothingBtn);
        grid.add(viewCartBtn);
        grid.add(checkoutBtn);
        grid.add(setBudgetBtn);
        grid.add(reservationsBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(topRow, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        panel.add(logoutBtn, BorderLayout.SOUTH);
        return panel;
    }

    
    private JPanel buildElectronicsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);
        JLabel h = new JLabel("Electronics");
        h.setFont(heading); h.setForeground(brandBlue); h.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(h, BorderLayout.NORTH);

        DefaultListModel<Product> model = new DefaultListModel<>();
        JList<Product> list = new JList<>(model);
        
        list.setCellRenderer(new ZebraListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        
        Runnable refresh = () -> {
            model.clear();
            for (Product p : manager.getInventory()) {
                if ("Electronics".equals(p.getCategory())) {
                    if (!budgetSet || p.getPrice() <= userBudget) model.addElement(p);
                }
            }
        };
        refresh.run();

        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton add = makeButton("Add to Cart");
        JButton reserve = makeButton("Reserve");
        JButton back = new JButton("Back"); back.setFont(normal);
        
        back.setBackground(buttonColor);
        back.setForeground(Color.WHITE);
        back.setRolloverEnabled(false);
        
        JButton viewCart = makeButton("View Cart");

        add.addActionListener(e -> {
            Product sel = list.getSelectedValue();
            if (sel==null) { JOptionPane.showMessageDialog(this,"Select a product first."); return; }

            
            double current = manager.getCart().total();
            double wouldBe = current + sel.getPrice();
            if (budgetSet && wouldBe > userBudget) {
                JOptionPane.showMessageDialog(this,
                        "Cannot add — exceeds your budget.\n" +
                        "Item price: AED " + money.format(sel.getPrice()) +
                        "\nCurrent total: AED " + money.format(current) +
                        "\nBudget: AED " + money.format(userBudget));
                return;
            }

            manager.getCart().add(sel);
            updateBudgetUI();
            JOptionPane.showMessageDialog(this, sel.getName() + " added to cart.");
        });

        reserve.addActionListener(e -> {
            Product sel = list.getSelectedValue();
            if (sel==null) { JOptionPane.showMessageDialog(this,"Select a product to reserve."); return; }
            
            String s = JOptionPane.showInputDialog(this,
                    "Enter planned purchase date (YYYY-MM-DD):", LocalDate.now().plusDays(7).toString());
            if (s==null) return;
            try {
                LocalDate planned = LocalDate.parse(s.trim());
                if (planned.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this,"Planned date must be today or in future.");
                    return;
                }
                
                double fee = Math.max(5.0, Math.round(sel.getPrice() * 0.10 * 100.0) / 100.0);
                Reservation r = new Reservation(sel, LocalDate.now(), planned, fee);
                manager.addReservation(r);
                JOptionPane.showMessageDialog(this, "Reserved " + sel.getName() + ". Reservation fee: AED " + money.format(fee));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            }
        });

        back.addActionListener(e -> cards.show(root, "home"));
        viewCart.addActionListener(e -> cards.show(root, "cart"));

        JPanel btns = new JPanel(); btns.setOpaque(false);
        btns.add(add);
        btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(reserve);
        btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(viewCart);
        btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(back);
        btns.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(btns, BorderLayout.SOUTH);

        
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) { refresh.run(); }
        });

        return panel;
    }

    
    private JPanel buildClothingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);
        JLabel h = new JLabel("Clothing");
        h.setFont(heading); h.setForeground(brandBlue); h.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(h, BorderLayout.NORTH);

        DefaultListModel<Product> model = new DefaultListModel<>();
        JList<Product> list = new JList<>(model);
        
        list.setCellRenderer(new ZebraListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        
        Runnable refresh = () -> {
            model.clear();
            for (Product p : manager.getInventory()) {
                if ("Clothing".equals(p.getCategory())) {
                    if (!budgetSet || p.getPrice() <= userBudget) model.addElement(p);
                }
            }
        };
        refresh.run();

        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton add = makeButton("Add to Cart");
        JButton reserve = makeButton("Reserve");
        JButton back = new JButton("Back"); back.setFont(normal);
        
        back.setBackground(buttonColor);
        back.setForeground(Color.WHITE);
        back.setRolloverEnabled(false);
        
        JButton viewCart = makeButton("View Cart");

        add.addActionListener(e -> {
            Product sel = list.getSelectedValue();
            if (sel==null) { JOptionPane.showMessageDialog(this,"Select a product first."); return; }

            
            double current = manager.getCart().total();
            double wouldBe = current + sel.getPrice();
            if (budgetSet && wouldBe > userBudget) {
                JOptionPane.showMessageDialog(this,
                        "Cannot add — exceeds your budget.\n" +
                        "Item price: AED " + money.format(sel.getPrice()) +
                        "\nCurrent total: AED " + money.format(current) +
                        "\nBudget: AED " + money.format(userBudget));
                return;
            }

            manager.getCart().add(sel);
            updateBudgetUI();
            JOptionPane.showMessageDialog(this, sel.getName() + " added to cart.");
        });

        reserve.addActionListener(e -> {
            Product sel = list.getSelectedValue();
            if (sel==null) { JOptionPane.showMessageDialog(this,"Select a product to reserve."); return; }
           
            String s = JOptionPane.showInputDialog(this,
                    "Enter planned purchase date (YYYY-MM-DD):", LocalDate.now().plusDays(7).toString());
            if (s==null) return;
            try {
                LocalDate planned = LocalDate.parse(s.trim());
                if (planned.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this,"Planned date must be today or in future.");
                    return;
                }
                
                double fee = Math.max(5.0, Math.round(sel.getPrice() * 0.10 * 100.0) / 100.0);
                Reservation r = new Reservation(sel, LocalDate.now(), planned, fee);
                manager.addReservation(r);
                JOptionPane.showMessageDialog(this, "Reserved " + sel.getName() + ". Reservation fee: AED " + money.format(fee));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            }
        });

        back.addActionListener(e -> cards.show(root, "home"));
        viewCart.addActionListener(e -> cards.show(root, "cart"));

        JPanel btns = new JPanel(); btns.setOpaque(false);
        btns.add(add);
        btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(reserve);
        btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(viewCart);
        btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(back);
        btns.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(btns, BorderLayout.SOUTH);

        
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) { refresh.run(); }
        });

        return panel;
    }

    
    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);
        JLabel h = new JLabel("Your Cart");
        h.setFont(heading); h.setForeground(brandBlue);
        h.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(h, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
       
        list.setCellRenderer(new ZebraListRenderer());

        
        Runnable refresh = () -> {
            model.clear();
            for (Product p : manager.getCart().getAll()) {
                model.addElement(p.getId() + " • " + p.getName() + " — AED " + money.format(p.getPrice()));
            }
            updateBudgetUI();
        };

        JButton remove = makeButton("Remove Selected");
        JButton checkout = makeButton("Checkout");
        JButton back = new JButton("Back"); back.setFont(normal);
        
        back.setBackground(buttonColor);
        back.setForeground(Color.WHITE);
        back.setRolloverEnabled(false);

        remove.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel==null) { JOptionPane.showMessageDialog(this,"Select item to remove."); return; }
            String id = sel.split(" • ")[0];
            manager.getCart().remove(id);
            refresh.run();
            JOptionPane.showMessageDialog(this,"Removed.");
        });

        checkout.addActionListener(e -> {
            if (manager.getCart().isEmpty()) { JOptionPane.showMessageDialog(this,"Cart is empty."); return; }
            cards.show(root, "checkout");
        });

        back.addActionListener(e -> cards.show(root, "home"));

        JPanel btns = new JPanel(); btns.setOpaque(false);
        btns.add(remove);
        btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(checkout);
        btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(back);
        btns.setBorder(new EmptyBorder(8,8,8,8));

        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) { refresh.run(); }
        });

        return panel;
    }
    private JPanel buildCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);
        JLabel h = new JLabel("Checkout");
        h.setFont(heading); h.setForeground(brandBlue); h.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(h, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(12,12,12,12));

        JTextField nameField = new JTextField(); nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        JTextField addressField = new JTextField(); addressField.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        
        
        JPanel phonePanel = new JPanel(new BorderLayout());
        phonePanel.setOpaque(false);
        phonePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        
        JLabel countryCodeLabel = new JLabel("+971 ");
        countryCodeLabel.setFont(normal);
        countryCodeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        
        JTextField phoneField = new JTextField();
        applyDigitFilter(phoneField, 9); 
        
        phonePanel.add(countryCodeLabel, BorderLayout.WEST);
        phonePanel.add(phoneField, BorderLayout.CENTER);

        String[] paymentOptions = {"Cash on Delivery", "Card Payment", "UPI"};
        JComboBox<String> payBox = new JComboBox<>(paymentOptions);
        payBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));

        center.add(new JLabel("Full name:")); center.add(nameField); center.add(Box.createRigidArea(new Dimension(0,8)));
        center.add(new JLabel("Delivery address:")); center.add(addressField); center.add(Box.createRigidArea(new Dimension(0,8)));
        center.add(new JLabel("Phone number:")); center.add(phonePanel); center.add(Box.createRigidArea(new Dimension(0,8)));
        center.add(new JLabel("Payment method:")); center.add(payBox); center.add(Box.createRigidArea(new Dimension(0,12)));
        JTextField cardNumberField = new JTextField(); cardNumberField.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        JTextField cardExpiryField = new JTextField(); cardExpiryField.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        JTextField cardCVVField = new JTextField(); cardCVVField.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        JTextField upiField = new JTextField(); upiField.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        applyDigitFilter(cardNumberField, 16); 
        applyDigitFilter(cardCVVField, 3);     
        applyDigitFilter(upiField, 20);        
        applyExpiryFilter(cardExpiryField); 

        JPanel cardDetailsPanel = new JPanel();
        cardDetailsPanel.setOpaque(false);
        cardDetailsPanel.setLayout(new BoxLayout(cardDetailsPanel, BoxLayout.Y_AXIS));
        cardDetailsPanel.add(new JLabel("Card Number (16 digits):")); cardDetailsPanel.add(cardNumberField);
        cardDetailsPanel.add(new JLabel("Expiry (MM/YY):")); cardDetailsPanel.add(cardExpiryField);
        cardDetailsPanel.add(new JLabel("CVV (3 digits):")); cardDetailsPanel.add(cardCVVField);

        JPanel upiDetailsPanel = new JPanel();
        upiDetailsPanel.setOpaque(false);
        upiDetailsPanel.setLayout(new BoxLayout(upiDetailsPanel, BoxLayout.Y_AXIS));
        upiDetailsPanel.add(new JLabel("UPI ID (digits only):")); upiDetailsPanel.add(upiField);

        cardDetailsPanel.setVisible(false);
        upiDetailsPanel.setVisible(false);

        payBox.addActionListener(e -> {
            String selected = (String) payBox.getSelectedItem();
            cardDetailsPanel.setVisible("Card Payment".equals(selected));
            upiDetailsPanel.setVisible("UPI".equals(selected));
        });
        
        center.add(cardDetailsPanel);
        center.add(upiDetailsPanel);

        JTextArea summaryArea = new JTextArea(8,40);
        summaryArea.setEditable(false); summaryArea.setFont(normal);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(new LineBorder(Color.LIGHT_GRAY,1,true));
        center.add(new JLabel("Order summary:"));
        center.add(summaryScroll);

        JPanel btnRow = new JPanel(); btnRow.setOpaque(false);
        JButton showSummary = makeButton("Generate Summary");
        JButton placeOrder = makeButton("Place Order");
        JButton back = new JButton("Back"); back.setFont(normal);
        back.setBackground(buttonColor);
        back.setForeground(Color.WHITE);
        back.setRolloverEnabled(false);

        showSummary.addActionListener(e -> {
            if (manager.getCart().isEmpty()) { JOptionPane.showMessageDialog(this,"Cart is empty."); return; }
            StringBuilder sb = new StringBuilder();
            sb.append("Items:\n");
            for (Product p : manager.getCart().getAll())
                sb.append("- ").append(p.getName())
                  .append(" (AED ").append(money.format(p.getPrice())).append(")\n");

            double total = manager.getCart().total();
            String pay = (String) payBox.getSelectedItem();
            if ("Cash on Delivery".equals(pay)) total += COD_FEE;

            sb.append("\nTotal: AED ").append(money.format(total));
            summaryArea.setText(sb.toString());
        });

        placeOrder.addActionListener(e -> {
            if (manager.getCart().isEmpty()) { JOptionPane.showMessageDialog(this,"Cart is empty."); return; }
            String name = nameField.getText().trim();
            String addr = addressField.getText().trim();
            String phone = phoneField.getText().trim();
            String pay = (String) payBox.getSelectedItem();

            if (name.isEmpty() || addr.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this,"Please fill delivery details.");
                return;
            }
            if (!phone.matches("^\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Phone must be 9 digits after +971.");
                return;
            }

            if ("Card Payment".equals(pay)) {
                if (cardNumberField.getText().trim().isEmpty()
                        || cardExpiryField.getText().trim().isEmpty()
                        || cardCVVField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all card details.");
                    return;
                }
                String cardNum = cardNumberField.getText().trim();
                String cvv = cardCVVField.getText().trim();
                String exp = cardExpiryField.getText().trim();
                if (!cardNum.matches("^\\d{16}$")) {
                    JOptionPane.showMessageDialog(this, "Card number must be 16 digits.");
                    return;
                }
                if (!cvv.matches("^\\d{3}$")) {
                    JOptionPane.showMessageDialog(this, "CVV must be 3 digits.");
                    return;
                }
                if (!isValidExpiry(exp)) {
                    JOptionPane.showMessageDialog(this, "Expiry must be in MM/YY format and valid.");
                    return;
                }
            } else if ("UPI".equals(pay)) {
                if (upiField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill UPI ID.");
                    return;
                }
                String upi = upiField.getText().trim();
                if (!upi.matches("^\\d{6,}$")) {
                    JOptionPane.showMessageDialog(this, "UPI ID must be digits (min 6).");
                    return;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("ORDER CONFIRMATION\n\n")
              .append("Name: ").append(name).append("\n")
              .append("Address: ").append(addr).append("\n")
              .append("Phone: +971 ").append(phone).append("\n\n")
              .append("Items:\n");
            for (Product p : manager.getCart().getAll())
                sb.append("- ").append(p.getName())
                  .append(" (AED ").append(money.format(p.getPrice())).append(")\n");

            double total = manager.getCart().total();
            if ("Cash on Delivery".equals(pay)) {
                sb.append("\nPayment: Cash on Delivery (+AED ")
                  .append(money.format(COD_FEE)).append(" fee)\n");
                total += COD_FEE;
            } else if ("Card Payment".equals(pay)) {
                sb.append("\nPayment: Card Payment\n");
                sb.append("Card Number: ").append(maskCard(cardNumberField.getText().trim())).append("\n");
                sb.append("Expiry: ").append(cardExpiryField.getText().trim()).append("\n");
            } else if ("UPI".equals(pay)) {
                sb.append("\nPayment: UPI\n");
                sb.append("UPI ID: ").append(upiField.getText().trim()).append("\n");
            }

            sb.append("Total: AED ").append(money.format(total)).append("\n\n");
            sb.append("Thank you for your order!");

            JOptionPane.showMessageDialog(this, sb.toString(), "Order Placed",
                    JOptionPane.INFORMATION_MESSAGE);
            manager.getCart().clear();
            nameField.setText(""); addressField.setText(""); phoneField.setText(""); summaryArea.setText("");
            cardNumberField.setText(""); cardExpiryField.setText(""); cardCVVField.setText(""); upiField.setText("");
            budgetSet = false; userBudget = 0.0; updateBudgetUI();
            cards.show(root, "home");
        });

        back.addActionListener(e -> cards.show(root, "home"));

        btnRow.add(showSummary);
        btnRow.add(Box.createRigidArea(new Dimension(8,0)));
        btnRow.add(placeOrder);
        btnRow.add(Box.createRigidArea(new Dimension(8,0)));
        btnRow.add(back);
        center.add(Box.createRigidArea(new Dimension(0,12)));
        center.add(btnRow);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private static boolean isValidExpiry(String mmYY) {
        if (!mmYY.matches("^\\d{2}/\\d{2}$")) return false;
        try {
            int mm = Integer.parseInt(mmYY.substring(0,2));
            int yy = Integer.parseInt(mmYY.substring(3,5));
            if (mm < 1 || mm > 12) return false;
            return true;
        } catch (Exception ex) { return false; }
    }

    private static String maskCard(String card) {
        if (card.length() < 4) return card;
        return "**** **** **** " + card.substring(card.length()-4);
    }
    private JPanel buildReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(softBlue);
        JLabel h = new JLabel("Your Reservations");
        h.setFont(heading); h.setForeground(brandBlue); h.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(h, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setCellRenderer(new ZebraListRenderer());

        Runnable refresh = () -> {
            model.clear();
            java.util.List<Reservation> all = manager.getReservations();
            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
            int i=1;
            for (Reservation r : all) {
                String status = r.cancelled ? "CANCELLED" : (r.purchased ? "PURCHASED" : "ACTIVE");
                String line = String.format("%d) [%s] %s — AED %s | Reserved: %s | Planned: %s | Fee: AED %s",
                        i++,
                        status,
                        r.product.getName(),
                        money.format(r.product.getPrice()),
                        r.reservationDate.format(fmt),
                        r.plannedPurchaseDate.format(fmt),
                        money.format(r.fee));
                model.addElement(line);
            }
        };
        refresh.run();

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(new LineBorder(Color.LIGHT_GRAY,1,true));
        panel.add(scroll, BorderLayout.CENTER);

        JButton cancelBtn = makeButton("Cancel Reservation");
        JButton purchaseNow = makeButton("Purchase Now (move to cart)");
        JButton details = makeButton("View Details");
        JButton back = new JButton("Back"); back.setFont(normal);
        back.setBackground(buttonColor);
        back.setForeground(Color.WHITE);
        back.setRolloverEnabled(false);

        cancelBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(this, "Select a reservation first."); return; }
            Reservation r = manager.getReservations().get(idx);
            if (r.cancelled) { JOptionPane.showMessageDialog(this, "Already cancelled."); return; }
            if (r.purchased) { JOptionPane.showMessageDialog(this, "Already purchased."); return; }

            long days = ChronoUnit.DAYS.between(LocalDate.now(), r.plannedPurchaseDate);
            double refund = 0.0;
            if (days > 7) {
                refund = r.fee; 
            } else if (days >= 0) {
                refund = r.fee * 0.5; 
            } else {
                refund = 0.0; 
            }
            r.cancelled = true;
            JOptionPane.showMessageDialog(this, String.format("Reservation cancelled. Refund: AED %s", money.format(refund)));
            refresh.run();
        });

        purchaseNow.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(this, "Select a reservation first."); return; }
            Reservation r = manager.getReservations().get(idx);
            if (r.cancelled) { JOptionPane.showMessageDialog(this, "Reservation cancelled — cannot purchase."); return; }
            if (r.purchased) { JOptionPane.showMessageDialog(this, "Already purchased."); return; }
             manager.getCart().add(r.product);
            r.purchased = true;
            r.purchaseDate = LocalDate.now();
            updateBudgetUI();
            JOptionPane.showMessageDialog(this, r.product.getName() + " moved to cart. Proceed to checkout to complete purchase.");
            refresh.run();
        });

        details.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(this, "Select a reservation first."); return; }
            Reservation r = manager.getReservations().get(idx);
            StringBuilder sb = new StringBuilder();
            sb.append("Product: ").append(r.product.getName()).append("\n");
            sb.append("Price: AED ").append(money.format(r.product.getPrice())).append("\n");
            sb.append("Reserved on: ").append(r.reservationDate).append("\n");
            sb.append("Planned purchase date: ").append(r.plannedPurchaseDate).append("\n");
            sb.append("Reservation fee: AED ").append(money.format(r.fee)).append("\n");
            sb.append("Status: ").append(r.cancelled ? "CANCELLED" : r.purchased ? "PURCHASED" : "ACTIVE").append("\n");
            if (r.purchased) sb.append("Purchased on: ").append(r.purchaseDate).append("\n");
            JOptionPane.showMessageDialog(this, sb.toString());
        });

        back.addActionListener(e -> cards.show(root, "home"));

        JPanel btns = new JPanel(); btns.setOpaque(false);
        btns.add(details); btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(purchaseNow); btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(cancelBtn); btns.add(Box.createRigidArea(new Dimension(8,0)));
        btns.add(back);
        btns.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(btns, BorderLayout.SOUTH);

        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) { refresh.run(); }
        });

        return panel;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartCartApp::new);
    }
}
class SmartCartManager {

    private final java.util.List<SmartCartApp.Product> inventory = new ArrayList<>();
    private final java.util.List<String> usernames = new ArrayList<>();
    private final java.util.List<String> passwords = new ArrayList<>();
    private final SmartCartApp.Cart cart = new SmartCartApp.Cart();
    private final java.util.List<SmartCartApp.Reservation> reservations = new ArrayList<>();

    public SmartCartManager() {
        initData();
    }
    private void initData() {
        usernames.add("user"); passwords.add("user123");
        usernames.add("admin"); passwords.add("admin123");
        inventory.add(new SmartCartApp.Electronics("E101","Samsung Galaxy Buds 2", 249));
        inventory.add(new SmartCartApp.Electronics("E102","Apple iPad 10th Gen (64GB)", 1499));
        inventory.add(new SmartCartApp.Electronics("E103","Sony WH-1000XM4", 999));
        inventory.add(new SmartCartApp.Electronics("E104","Logitech MX Master 3S Mouse", 299));
        inventory.add(new SmartCartApp.Electronics("E105","Anker PowerCore 20000mAh", 129));
        inventory.add(new SmartCartApp.Electronics("E106","Apple iPhone 15 Pro", 3999));
        inventory.add(new SmartCartApp.Electronics("E107","Samsung Galaxy S24 Ultra", 4299));
        inventory.add(new SmartCartApp.Electronics("E108","MacBook Air M2 13\"", 4999));
        inventory.add(new SmartCartApp.Electronics("E109","Dell G15 Gaming Laptop", 3899));
        inventory.add(new SmartCartApp.Electronics("E110","Sony WH-1000XM5", 1399));
        inventory.add(new SmartCartApp.Electronics("E111","JBL Flip 6 Bluetooth Speaker", 399));
        inventory.add(new SmartCartApp.Electronics("E112","Google Pixel 8a", 1699));
        inventory.add(new SmartCartApp.Clothing("C201","Nike Air Max T-Shirt", 99));
        inventory.add(new SmartCartApp.Clothing("C202","Adidas Joggers", 149));
        inventory.add(new SmartCartApp.Clothing("C203","Zara Women's Top", 89));
        inventory.add(new SmartCartApp.Clothing("C204","H&M Hoodie", 119));
        inventory.add(new SmartCartApp.Clothing("C205","Levi's 511 Jeans", 199));
        inventory.add(new SmartCartApp.Clothing("C206","Men's Classic Hoodie", 149));
        inventory.add(new SmartCartApp.Clothing("C207","Women's Lightweight Jacket", 199));
        inventory.add(new SmartCartApp.Clothing("C208","Sneakers (Unisex)", 259));
        inventory.add(new SmartCartApp.Clothing("C209","Sports T-Shirt", 89));
        inventory.add(new SmartCartApp.Clothing("C210","Formal Shirt", 129));
        inventory.add(new SmartCartApp.Clothing("C211","Slim Fit Jeans", 159));
        inventory.add(new SmartCartApp.Clothing("C212","Summer Dress", 149));
    }
    public java.util.List<SmartCartApp.Product> getInventory() { return inventory; }
    public SmartCartApp.Cart getCart() { return cart; }
    public java.util.List<String> getUsernames() { return usernames; }
    public java.util.List<String> getPasswords() { return passwords; }
    public java.util.List<SmartCartApp.Reservation> getReservations() { return reservations; }
    public void addReservation(SmartCartApp.Reservation r) {
        reservations.add(r);
    }
}
