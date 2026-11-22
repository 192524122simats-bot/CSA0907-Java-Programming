import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/*
 * Programming in Java - Assignment 4 (CO4)
 * Consolidated, single-file demonstrations of the GUI/event-driven designs
 * in the supplied PDF. This standalone version uses in-memory sample data
 * for payment, appointments and registrations (no external database required).
 */
public class Assignment4 extends JFrame {
    public Assignment4() {
        super("Java Assignment 4 - Event-Driven GUI Design");
        setLayout(new BorderLayout(10,10));
        JLabel title = new JLabel("Java Assignment 4 - GUI Demonstrations", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);
        JPanel menu = new JPanel(new GridLayout(0,1,8,8));
        String[] options = {"Task 1 - Fee Payment", "Task 2 - Appointment Desk", "Task 3 - Library Search",
                "Task 4 - Draw Pad", "Task 5 - Currency Converter", "Task 6 - Event Registration"};
        for (String option : options) {
            JButton b = new JButton(option);
            b.addActionListener(e -> openDemo(option));
            menu.add(b);
        }
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));
        wrapper.add(menu, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520,420);
        setLocationRelativeTo(null);
    }
    private void openDemo(String option) {
        if (option.startsWith("Task 1")) new FeePayment().setVisible(true);
        else if (option.startsWith("Task 2")) new AppointmentDesk().setVisible(true);
        else if (option.startsWith("Task 3")) new LibrarySearch().setVisible(true);
        else if (option.startsWith("Task 4")) new DrawPad().setVisible(true);
        else if (option.startsWith("Task 5")) new CurrencyConverter().setVisible(true);
        else new EventRegistration().setVisible(true);
    }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new Assignment4().setVisible(true)); }

    // TASK 1: Fee payment demo (sample in-memory operation; JDBC can be added)
    static class FeePayment extends JFrame {
        JTextField regNo = new JTextField(12), amount = new JTextField(10);
        JLabel status = new JLabel("Enter payment details");
        FeePayment() {
            super("Fee Payment"); setLayout(new FlowLayout());
            add(new JLabel("Registration No:")); add(regNo);
            add(new JLabel("Amount:")); add(amount);
            JButton pay = new JButton("Pay"), clear = new JButton("Clear"), receipt = new JButton("Receipt");
            add(pay); add(clear); add(receipt); add(status);
            pay.addActionListener(e -> pay());
            clear.addActionListener(e -> { regNo.setText(""); amount.setText(""); status.setText("Fields cleared"); });
            receipt.addActionListener(e -> JOptionPane.showMessageDialog(this, "Latest payment: " + status.getText()));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(430,180); setLocationRelativeTo(null);
        }
        void pay() {
            if (regNo.getText().trim().isEmpty()) { status.setText("Registration number is required"); return; }
            try {
                double a = Double.parseDouble(amount.getText().trim());
                if (!Double.isFinite(a) || a <= 0) { status.setText("Amount must be greater than zero"); return; }
                status.setText("Payment successful for " + regNo.getText().trim() + " : Rs. " + a);
            } catch (NumberFormatException ex) { status.setText("Enter a valid amount"); }
        }
    }

    // TASK 2: Appointment booking, with doctors refreshed by department
    static class AppointmentDesk extends JFrame {
        JTextField patientId = new JTextField(12);
        JComboBox<String> department = new JComboBox<>(new String[]{"Cardiology","Neurology","General"});
        JComboBox<String> doctor = new JComboBox<>(), slot = new JComboBox<>(new String[]{"10:00 AM","11:00 AM","02:00 PM"});
        JLabel status = new JLabel("Select details and book");
        Map<String,String[]> doctors = new LinkedHashMap<>();
        AppointmentDesk() {
            super("Appointment Desk"); doctors.put("Cardiology",new String[]{"Dr. Arun","Dr. Meena"});
            doctors.put("Neurology",new String[]{"Dr. Kumar","Dr. Priya"}); doctors.put("General",new String[]{"Dr. Ravi","Dr. Latha"});
            setLayout(new GridLayout(0,2,8,8));
            add(new JLabel("Patient ID:")); add(patientId); add(new JLabel("Department:")); add(department);
            add(new JLabel("Doctor:")); add(doctor); add(new JLabel("Time slot:")); add(slot);
            JButton book = new JButton("Book"); add(book); add(status);
            department.addActionListener(e -> refreshDoctors()); refreshDoctors();
            book.addActionListener(e -> bookAppointment());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(430,230); setLocationRelativeTo(null);
        }
        void refreshDoctors() { doctor.removeAllItems(); String[] list=doctors.get(department.getSelectedItem()); if(list!=null) for(String d:list) doctor.addItem(d); }
        void bookAppointment() {
            if(patientId.getText().trim().isEmpty() || department.getSelectedItem()==null || doctor.getSelectedItem()==null || slot.getSelectedItem()==null) {
                status.setText("Complete all required fields"); return;
            }
            status.setText("Appointment booked: " + patientId.getText().trim()+", "+doctor.getSelectedItem()+", "+slot.getSelectedItem());
        }
    }

    // TASK 3: Library Search GUI
    static class LibrarySearch extends JFrame {
        JTextField searchField = new JTextField(18); JComboBox<String> category = new JComboBox<>(new String[]{"All","Books","Journals","Theses"});
        JLabel status = new JLabel("Ready"); DefaultTableModel model;
        LibrarySearch() {
            super("Campus Library Search"); setLayout(new BorderLayout(8,8));
            JLabel title = new JLabel("Campus Library Search",SwingConstants.CENTER);
            JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT)); JButton search=new JButton("Search");
            searchRow.add(searchField); searchRow.add(category); searchRow.add(search);
            JPanel north=new JPanel(new BorderLayout()); north.add(title,BorderLayout.NORTH); north.add(searchRow,BorderLayout.SOUTH); add(north,BorderLayout.NORTH);
            model=new DefaultTableModel(new String[]{"Book ID","Title","Author","Category"},0);
            model.addRow(new Object[]{"B101","Java Programming","Herbert Schildt","Books"});
            model.addRow(new Object[]{"J102","Data Science Review","Editorial Team","Journals"});
            model.addRow(new Object[]{"T103","AI Research","Student Author","Theses"});
            JTable table=new JTable(model); add(new JScrollPane(table),BorderLayout.CENTER);
            JButton prev=new JButton("Prev"),next=new JButton("Next"),reserve=new JButton("Reserve");
            JPanel buttons=new JPanel(); buttons.add(prev); buttons.add(next); buttons.add(reserve);
            JPanel south=new JPanel(new BorderLayout()); south.add(buttons,BorderLayout.NORTH); south.add(status,BorderLayout.SOUTH); add(south,BorderLayout.SOUTH);
            search.addActionListener(e -> status.setText("Search requested: "+searchField.getText()));
            prev.addActionListener(e -> status.setText("Previous results")); next.addActionListener(e -> status.setText("Next results"));
            reserve.addActionListener(e -> { if(table.getSelectedRow()>=0) status.setText("Selected for reservation: "+table.getValueAt(table.getSelectedRow(),1)); else status.setText("Select a result to reserve"); });
            setDefaultCloseOperation(DISPOSE_ON_CLOSE); setMinimumSize(new Dimension(520,320)); setSize(720,480); setLocationRelativeTo(null);
        }
    }

    // TASK 4: DrawPad; points are retained and redrawn after repaint
    static class DrawPad extends JFrame {
        java.util.List<Point> points = new ArrayList<>(); JLabel status = new JLabel("Clicks: 0"); JPanel canvas;
        DrawPad() {
            super("Draw Pad"); setLayout(new BorderLayout()); add(status,BorderLayout.SOUTH);
            canvas=new JPanel() { protected void paintComponent(Graphics g) { super.paintComponent(g); for(Point p:points) g.fillOval(p.x-3,p.y-3,6,6); } };
            canvas.setBackground(Color.WHITE); add(canvas,BorderLayout.CENTER);
            canvas.addMouseListener(new MouseAdapter(){ @Override public void mouseClicked(MouseEvent e){ points.add(e.getPoint()); status.setText("Clicks: "+points.size()); canvas.repaint(); }});
            setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(400,300); setLocationRelativeTo(null);
        }
    }

    // TASK 5: Currency converter, lambda listener shared by button and Enter key
    static class CurrencyConverter extends JFrame {
        JTextField amount=new JTextField(10); JComboBox<String> target=new JComboBox<>(new String[]{"USD","EUR"}); JLabel result=new JLabel("Result");
        CurrencyConverter() {
            super("Currency Converter"); setLayout(new FlowLayout()); add(new JLabel("Amount in INR:")); add(amount); add(target);
            JButton convertButton=new JButton("Convert"); add(convertButton); add(result);
            ActionListener handler=e -> convert(); convertButton.addActionListener(handler); amount.addActionListener(handler);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE); pack(); setLocationRelativeTo(null);
        }
        double rate(String currency){ return currency.equals("USD")?0.012:0.011; }
        void convert(){ try { double v=Double.parseDouble(amount.getText().trim()); if(!Double.isFinite(v)||v<0) throw new NumberFormatException(); String c=(String)target.getSelectedItem(); String m=String.format("%.2f %s",v*rate(c),c); result.setText(m); JOptionPane.showMessageDialog(this,m); } catch(NumberFormatException ex){ result.setText("Invalid amount"); JOptionPane.showMessageDialog(this,"Invalid amount","Input Error",JOptionPane.ERROR_MESSAGE); } }
    }

    // TASK 6: Event registration, filtered table and CSV export using sample data
    static class EventRegistration extends JFrame {
        JTextField studentId=new JTextField(10), studentName=new JTextField(12);
        JComboBox<String> eventCombo=new JComboBox<>(new String[]{"E101 - Tech Fest","E102 - Workshop","E103 - Cultural Event"});
        JComboBox<String> filter=new JComboBox<>(new String[]{"All Events","E101 - Tech Fest","E102 - Workshop","E103 - Cultural Event"});
        DefaultTableModel model=new DefaultTableModel(new String[]{"Student ID","Student Name","Event"},0);
        java.util.List<String[]> records=new ArrayList<>(); JLabel status=new JLabel("Ready");
        EventRegistration(){
            super("Campus Event Registration"); setLayout(new BorderLayout(8,8));
            JPanel form=new JPanel(new GridLayout(0,2,6,6)); form.add(new JLabel("Student ID:")); form.add(studentId); form.add(new JLabel("Student Name:")); form.add(studentName); form.add(new JLabel("Event:")); form.add(eventCombo);
            JButton register=new JButton("Register"); form.add(register); form.add(status); add(form,BorderLayout.NORTH);
            JTable table=new JTable(model); add(new JScrollPane(table),BorderLayout.CENTER);
            JPanel bottom=new JPanel(); JButton applyFilter=new JButton("Apply Filter"),export=new JButton("Export CSV"); bottom.add(filter);bottom.add(applyFilter);bottom.add(export);add(bottom,BorderLayout.SOUTH);
            register.addActionListener(e->registerStudent()); applyFilter.addActionListener(e->refreshTable()); export.addActionListener(e->exportCsv());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(650,420); setLocationRelativeTo(null);
        }
        void registerStudent(){
            String id=studentId.getText().trim(), name=studentName.getText().trim(), event=(String)eventCombo.getSelectedItem();
            if(id.isEmpty()||name.isEmpty()||event==null){JOptionPane.showMessageDialog(this,"Complete all required fields.");return;}
            for(String[] r:records) if(r[0].equals(id)&&r[2].equals(event)){JOptionPane.showMessageDialog(this,"Student already registered for this event");return;}
            records.add(new String[]{id,name,event}); status.setText("Registration successful"); JOptionPane.showMessageDialog(this,"Registration successful"); refreshTable();
        }
        void refreshTable(){ model.setRowCount(0); String selected=(String)filter.getSelectedItem(); for(String[] r:records) if(selected.equals("All Events")||r[2].equals(selected)) model.addRow(r); status.setText(model.getRowCount()+" record(s) loaded"); }
        void exportCsv(){ if(model.getRowCount()==0){JOptionPane.showMessageDialog(this,"No records to export");return;} JFileChooser chooser=new JFileChooser(); chooser.setSelectedFile(new java.io.File("registrations.csv")); if(chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){ try(java.io.PrintWriter out=new java.io.PrintWriter(chooser.getSelectedFile())){ out.println("Student ID,Student Name,Event"); for(int i=0;i<model.getRowCount();i++) out.println(csv(model.getValueAt(i,0))+","+csv(model.getValueAt(i,1))+","+csv(model.getValueAt(i,2))); status.setText("CSV exported"); JOptionPane.showMessageDialog(this,"Export successful"); }catch(Exception ex){JOptionPane.showMessageDialog(this,"Export failed: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);} } }
        String csv(Object value){String s=String.valueOf(value).replace("\"","\"\"");return "\""+s+"\"";}
    }
}
