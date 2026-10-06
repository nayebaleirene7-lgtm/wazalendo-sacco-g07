package ug.ac.vu.g07.core;

import ug.ac.vu.g07.members.*;
import ug.ac.vu.g07.savings.*;
import ug.ac.vu.g07.shares.*;
import ug.ac.vu.g07.loans.*;
import ug.ac.vu.g07.meetings.*;

import java.util.List;
import java.util.Scanner;

/**
 * Main entry point - Wazalendo SACCO Management System
 * Group One (G07)
 *
 * @author Ssempewo Brian (Integration Lead)
 */
public class Main {

    private final Scanner scanner = new Scanner(System.in);
    private final InputHelper input = new InputHelper(scanner);

    private final MemberService memberService = new MemberService();
    private final SavingsService savingsService = new SavingsService(memberService);
    private final ShareService shareService = new ShareService(memberService);
    private final LoanService loanService = new LoanService(memberService, savingsService, shareService);
    private final MeetingService meetingService = new MeetingService(memberService);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("================================================");
        System.out.println("   WAZALENDO SACCO MANAGEMENT SYSTEM");
        System.out.println("================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = input.readMenuChoice(0, 8);
            System.out.println();
            switch (choice) {
                case 1 -> membersMenu();
                case 2 -> savingsMenu();
                case 3 -> loansMenu();          // Loan Management
                case 4 -> repayLoan();          // Loan Repayment Management (direct)
                case 5 -> sharesMenu();
                case 6 -> meetingsMenu();
                case 7 -> meetingsMenu();       // Welfare linked to Meetings & Fines
                case 8 -> systemSummary();      // Reports
                case 0 -> {
                    System.out.println("Thank you for using Wazalendo SACCO Management System.");
                    running = false;
                }
            }
            if (running) System.out.println();
        }
        scanner.close();
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("================================================");
        System.out.println("   WAZALENDO SACCO MANAGEMENT SYSTEM");
        System.out.println("================================================");
        System.out.println("1. Member Management");
        System.out.println("2. Savings Management");
        System.out.println("3. Loan Management");
        System.out.println("4. Loan Repayment Management");
        System.out.println("5. Shares Management");
        System.out.println("6. Meetings & Fines Management");
        System.out.println("7. Welfare Management");
        System.out.println("8. Reports");
        System.out.println("0. Exit");
    }

    // ==================== MEMBERS ====================
    private void membersMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- Member Management ---");
            System.out.println("  1. Register new member");
            System.out.println("  2. Find / List members");
            System.out.println("  3. Change member status");
            System.out.println("  0. Back");
            int c = input.readMenuChoice(0, 3);
            System.out.println();
            switch (c) {
                case 1 -> registerMember();
                case 2 -> { findMember(); listMembers(); }
                case 3 -> changeStatus();
                case 0 -> back = true;
            }
        }
    }

    private void registerMember() {
        try {
            String name = input.readNonEmptyString("Full name: ");
            String nin = input.readNonEmptyString("National ID (invented): ");
            String phone = input.readNonEmptyString("Phone: ");
            String kin = input.readNonEmptyString("Next of kin name: ");
            String kinPhone = input.readOptionalString("Next of kin phone: ");
            String id = memberService.generateId();
            Member m = new Member(id, name, nin, phone, kin, kinPhone,
                    java.time.LocalDate.now().toString());
            memberService.add(m);
            System.out.println("Member registered: " + m.getId() + " - " + m.getFullName());
        } catch (InvalidMemberException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void findMember() {
        String q = input.readNonEmptyString("Enter ID or part of name: ");
        Member byId = memberService.findById(q);
        if (byId != null) {
            System.out.println("Found: " + byId);
            return;
        }
        List<Member> list = memberService.findByName(q);
        if (list.isEmpty()) System.out.println("No members found.");
        else list.forEach(System.out::println);
    }

    private void listMembers() {
        List<Member> list = memberService.getAllSortedByName();
        if (list.isEmpty()) {
            System.out.println("No members yet.");
            return;
        }
        System.out.printf("%-10s %-25s %-12s %s%n", "ID", "Name", "Phone", "Status");
        for (Member m : list) {
            System.out.printf("%-10s %-25s %-12s %s%n",
                    m.getId(), m.getFullName(), m.getPhone(), m.getStatus());
        }
        System.out.println("Total: " + list.size());
    }

    private void changeStatus() {
        String id = input.readNonEmptyString("Member ID: ");
        Member m = memberService.findById(id);
        if (m == null) {
            System.out.println("Member not found.");
            return;
        }
        System.out.println("Current status: " + m.getStatus());
        System.out.println("1=ACTIVE  2=SUSPENDED  3=WITHDRAWN");
        int c = input.readMenuChoice(1, 3);
        m.setStatus(c == 1 ? Member.Status.ACTIVE : c == 2 ? Member.Status.SUSPENDED : Member.Status.WITHDRAWN);
        memberService.update(m);
        System.out.println("Status updated to " + m.getStatus());
    }

    // ==================== SAVINGS ====================
    private void savingsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- Savings Management ---");
            System.out.println("  1. Record deposit");
            System.out.println("  2. Record withdrawal");
            System.out.println("  3. View balance / history");
            System.out.println("  4. Savings report");
            System.out.println("  0. Back");
            int c = input.readMenuChoice(0, 4);
            System.out.println();
            switch (c) {
                case 1 -> doDeposit();
                case 2 -> doWithdrawal();
                case 3 -> viewSavings();
                case 4 -> savingsReport();
                case 0 -> back = true;
            }
        }
    }

    private void doDeposit() {
        try {
            String mid = input.readNonEmptyString("Member ID: ");
            double amt = input.readNonNegativeDouble("Amount (UGX): ");
            String note = input.readOptionalString("Note (optional): ");
            savingsService.deposit(mid, amt, note);
            System.out.println("Deposit OK. New balance: " +
                    String.format("%,.0f UGX", savingsService.getBalance(mid)));
        } catch (InvalidSavingsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void doWithdrawal() {
        try {
            String mid = input.readNonEmptyString("Member ID: ");
            double amt = input.readNonNegativeDouble("Amount (UGX): ");
            String note = input.readOptionalString("Note (optional): ");
            savingsService.withdraw(mid, amt, note);
            System.out.println("Withdrawal OK. New balance: " +
                    String.format("%,.0f UGX", savingsService.getBalance(mid)));
        } catch (InvalidSavingsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewSavings() {
        String mid = input.readNonEmptyString("Member ID: ");
        System.out.println("Balance: " + String.format("%,.0f UGX", savingsService.getBalance(mid)));
        List<SavingsTransaction> txns = savingsService.getTransactionsForMember(mid);
        if (txns.isEmpty()) System.out.println("No transactions.");
        else txns.forEach(t -> System.out.println("  " + t.describe()));
    }

    private void savingsReport() {
        System.out.println("Total savings in SACCO: " +
                String.format("%,.0f UGX", savingsService.getTotalSavings()));
        System.out.println("Top savers:");
        savingsService.getTopSavers(5).forEach(e ->
                System.out.println("  " + e.getKey() + " : " + String.format("%,.0f", e.getValue())));
    }

    // ==================== SHARES ====================
    private void sharesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- Shares Management ---");
            System.out.println("  1. Buy shares");
            System.out.println("  2. View shareholding");
            System.out.println("  3. Shares report");
            System.out.println("  0. Back");
            int c = input.readMenuChoice(0, 3);
            System.out.println();
            switch (c) {
                case 1 -> buyShares();
                case 2 -> viewShares();
                case 3 -> sharesReport();
                case 0 -> back = true;
            }
        }
    }

    private void buyShares() {
        try {
            String mid = input.readNonEmptyString("Member ID: ");
            int num = input.readPositiveInt("Number of shares: ");
            shareService.buyShares(mid, num);
            System.out.println("Shares purchased. Total shares: " + shareService.getTotalShares(mid) +
                    " | Capital: " + String.format("%,.0f UGX", shareService.getShareCapital(mid)));
        } catch (InvalidShareException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewShares() {
        String mid = input.readNonEmptyString("Member ID: ");
        System.out.println("Total shares : " + shareService.getTotalShares(mid));
        System.out.println("Share capital: " + String.format("%,.0f UGX", shareService.getShareCapital(mid)));
    }

    private void sharesReport() {
        System.out.println("Total share capital of SACCO: " +
                String.format("%,.0f UGX", shareService.getTotalShareCapital()));
        System.out.println("Ranking (highest first):");
        shareService.getRanking().forEach(e ->
                System.out.println("  " + e.getKey() + " : " + e.getValue() + " shares"));
    }

    // ==================== LOANS ====================
    private void loansMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- Loan Management ---");
            System.out.println("  1. Apply for loan");
            System.out.println("  2. Record repayment");
            System.out.println("  3. Mark overdue / view loans");
            System.out.println("  4. Active loans report");
            System.out.println("  0. Back");
            int c = input.readMenuChoice(0, 4);
            System.out.println();
            switch (c) {
                case 1 -> applyLoan();
                case 2 -> repayLoan();
                case 3 -> { markOverdue(); viewMemberLoans(); }
                case 4 -> activeLoansReport();
                case 0 -> back = true;
            }
        }
    }

    private void applyLoan() {
        try {
            String mid = input.readNonEmptyString("Member ID: ");
            double max = loanService.getMaxLoan(mid);
            System.out.println("Maximum you can borrow: " + String.format("%,.0f UGX", max));
            System.out.println("(Savings: " + String.format("%,.0f", savingsService.getBalance(mid)) +
                    " + Shares: " + String.format("%,.0f", shareService.getShareCapital(mid)) + ")");
            double principal = input.readNonNegativeDouble("Requested principal (UGX): ");
            System.out.println("Cycle: 1 = 3 months,  2 = 6 months");
            int choice = input.readMenuChoice(1, 2);
            int months = (choice == 1) ? 3 : 6;
            Loan loan = loanService.applyForLoan(mid, principal, months);
            System.out.println("Loan approved!");
            System.out.println("  Loan ID      : " + loan.getId());
            System.out.println("  Principal    : " + String.format("%,.0f", loan.getPrincipal()));
            System.out.println("  Interest 10% : " + String.format("%,.0f", loan.getInterestAmount()));
            System.out.println("  Total repay  : " + String.format("%,.0f", loan.getTotalRepayable()));
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void repayLoan() {
        try {
            String lid = input.readNonEmptyString("Loan ID: ");
            double amt = input.readNonNegativeDouble("Repayment amount (UGX): ");
            loanService.repay(lid, amt);
            Loan loan = loanService.findById(lid);
            System.out.println("Repayment recorded. Outstanding: " +
                    String.format("%,.0f UGX", loan.getOutstanding()) +
                    " | Status: " + loan.getStatus());
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void markOverdue() {
        try {
            String lid = input.readNonEmptyString("Loan ID: ");
            loanService.markOverdue(lid);
            Loan loan = loanService.findById(lid);
            System.out.println("Loan marked overdue. Outstanding now (with 2% penalty): " +
                    String.format("%,.0f UGX", loan.getOutstanding()));
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewMemberLoans() {
        String mid = input.readNonEmptyString("Member ID: ");
        List<Loan> list = loanService.getLoansForMember(mid);
        if (list.isEmpty()) System.out.println("No loans for this member.");
        else list.forEach(l -> System.out.println("  " + l.describe()));
    }

    private void activeLoansReport() {
        List<Loan> list = loanService.getActiveLoansSortedByOutstanding();
        if (list.isEmpty()) {
            System.out.println("No active loans.");
            return;
        }
        System.out.println("Active loans (highest outstanding first):");
        list.forEach(l -> System.out.println("  " + l.describe()));
        System.out.println("Total outstanding: " + String.format("%,.0f UGX", loanService.getTotalOutstanding()));
    }

    // ==================== MEETINGS & FINES ====================
    private void meetingsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- Meetings & Fines Management ---");
            System.out.println("  1. Create absence fine (2,000)");
            System.out.println("  2. Create late fine (500)");
            System.out.println("  3. Pay a fine");
            System.out.println("  4. View unpaid fines / summary");
            System.out.println("  0. Back");
            int c = input.readMenuChoice(0, 4);
            System.out.println();
            switch (c) {
                case 1 -> createAbsenceFine();
                case 2 -> createLateFine();
                case 3 -> payFine();
                case 4 -> { viewUnpaidFines(); finesSummary(); }
                case 0 -> back = true;
            }
        }
    }

    private void createAbsenceFine() {
        try {
            String mid = input.readNonEmptyString("Member ID: ");
            Fine f = meetingService.createAbsenceFine(mid);
            System.out.println("Absence fine created: " + f.describe());
        } catch (InvalidMeetingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void createLateFine() {
        try {
            String mid = input.readNonEmptyString("Member ID: ");
            Fine f = meetingService.createLateFine(mid);
            System.out.println("Late fine created: " + f.describe());
        } catch (InvalidMeetingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void payFine() {
        try {
            String fid = input.readNonEmptyString("Fine ID: ");
            meetingService.payFine(fid);
            System.out.println("Fine marked as PAID.");
        } catch (InvalidMeetingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewUnpaidFines() {
        String mid = input.readNonEmptyString("Member ID: ");
        List<Fine> list = meetingService.getUnpaidFinesForMember(mid);
        if (list.isEmpty()) System.out.println("No unpaid fines.");
        else {
            list.forEach(f -> System.out.println("  " + f.describe()));
            System.out.println("Total unpaid: " + String.format("%,.0f UGX",
                    meetingService.getUnpaidTotalForMember(mid)));
        }
    }

    private void finesSummary() {
        System.out.println("Total fines collected: " +
                String.format("%,.0f UGX", meetingService.getTotalFinesCollected()));
        System.out.println("All fines:");
        meetingService.getAllFines().forEach(f -> System.out.println("  " + f.describe()));
    }

    // ==================== SUMMARY / STATEMENT ====================
    private void systemSummary() {
        System.out.println("========== SYSTEM SUMMARY ==========");
        System.out.println("Members registered : " + memberService.count());
        System.out.println("Total savings      : " + String.format("%,.0f UGX", savingsService.getTotalSavings()));
        System.out.println("Total share capital: " + String.format("%,.0f UGX", shareService.getTotalShareCapital()));
        System.out.println("Active loan O/S    : " + String.format("%,.0f UGX", loanService.getTotalOutstanding()));
        System.out.println("Fines collected    : " + String.format("%,.0f UGX", meetingService.getTotalFinesCollected()));
        System.out.println("====================================");
        System.out.println();
        if (input.readYesNo("Print a Member Statement?")) {
            printMemberStatement();
        }
    }

    private void printMemberStatement() {
        String id = input.readNonEmptyString("Member ID: ");
        Member m = memberService.findById(id);
        if (m == null) {
            System.out.println("Member not found.");
            return;
        }
        System.out.println("\n========== MEMBER STATEMENT ==========");
        System.out.println("Member   : " + m.getFullName() + " (" + m.getId() + ")");
        System.out.println("Status   : " + m.getStatus());
        System.out.println("Phone    : " + m.getPhone());
        System.out.println("--------------------------------------");
        System.out.println("Savings balance : " + String.format("%,.0f UGX", savingsService.getBalance(id)));
        System.out.println("Share capital   : " + String.format("%,.0f UGX", shareService.getShareCapital(id)));
        System.out.println("Loan outstanding: " + String.format("%,.0f UGX", loanService.getOutstandingForMember(id)));
        System.out.println("Unpaid fines    : " + String.format("%,.0f UGX", meetingService.getUnpaidTotalForMember(id)));
        System.out.println("--------------------------------------");
        double maxLoan = loanService.getMaxLoan(id);
        System.out.println("Max loan now    : " + String.format("%,.0f UGX", maxLoan));
        System.out.println("======================================\n");
    }
}
