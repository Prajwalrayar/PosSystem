
import OrderInformation from "./OrderInformation";
import CustomerInformation from "./CustomerInformation";
import OrderItemTable from "../../../common/Order/OrderItemTable";
import { Card, CardContent } from "../../../../components/ui/card";

const OrderDetails = ({ selectedOrder }) => {
  return (
    <div className="space-y-3">
      <div className="grid grid-cols-1 gap-3 lg:grid-cols-2 [&>*]:min-w-0">
        <OrderInformation selectedOrder={selectedOrder} />
        <CustomerInformation selectedOrder={selectedOrder} />
      </div>

      <Card>
        <CardContent className="p-3 sm:p-4">
          <h3 className="mb-2 text-sm font-semibold sm:text-base">Order Items</h3>
          <OrderItemTable selectedOrder={selectedOrder} />
        </CardContent>
      </Card>
    </div>
  );
};

export default OrderDetails;
