export interface AdminDashboard {
  totalCustomers: number;
  totalInvoices: number;
  totalUsers: number;
  totalRevenue: number;
  todayInvoices: number;
  todayRevenue: number;
  todayPayments: number;
  todayNewCustomers: number;
  monthInvoices: number;
  monthRevenue: number;
  monthPayments: number;
  monthNewCustomers: number;
  totalPaid: number;
  totalUnpaid: number;
  paymentRate: number;
  processingStatusStats: ProcessingStatusStat[];
  last7DaysSales: DailyStat[];
  last12MonthsSales: MonthlyStat[];
  topCustomers: TopCustomer[];
  topServices: TopService[];
  recentInvoices: RecentInvoice[];
}

export interface UserDashboard {
  todayInvoices: number;
  todayRevenue: number;
  todayPayments: number;
  todayDeliveries: number;
  processingQueues: ProcessingQueue[];
  deliveriesToday: DeliveryToday[];
  pendingPayments: PendingPayment[];
  alerts: Alert[];
  recentInvoices: UserRecentInvoice[];
}

export interface ProcessingStatusStat {
  status: string;
  count: number;
  amount: number;
}

export interface DailyStat {
  date: string;
  invoiceCount: number;
  revenue: number;
}

export interface MonthlyStat {
  month: string;
  year: number;
  invoiceCount: number;
  revenue: number;
}

export interface TopCustomer {
  name: string;
  phone: string;
  invoiceCount: number;
  totalSpent: number;
}

export interface TopService {
  serviceName: string;
  quantity: number;
  revenue: number;
}

export interface RecentInvoice {
  invoiceNumber: string;
  customerName: string;
  depositDate: string;
  amount: number;
  status: string;
  paid: boolean;
}

export interface ProcessingQueue {
  status: string;
  statusLabel: string;
  count: number;
}

export interface DeliveryToday {
  invoiceNumber: string;
  customerName: string;
  customerPhone: string;
  amount: number;
  remainingAmount: number;
  processingStatus: string;
  ready: boolean;
}

export interface PendingPayment {
  invoiceNumber: string;
  customerName: string;
  customerPhone: string;
  depositDate: string;
  totalAmount: number;
  paidAmount: number;
  remainingAmount: number;
  daysOverdue: number;
}

export interface Alert {
  type: 'WARNING' | 'INFO' | 'DANGER';
  message: string;
  link: string;
}

export interface UserRecentInvoice {
  invoiceNumber: string;
  customerName: string;
  depositDate: string;
  deliveryDate: string;
  amount: number;
  processingStatus: string;
  paid: boolean;
}
