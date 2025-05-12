import { useState, useEffect } from 'react';
import { Card } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Button } from '@/components/ui/button';
import { X, Check } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { cn } from '@/lib/utils';
import {
  getNotifications,
  markNotificationAsRead,
  type Notification,
  NotificationType,
  NotificationPriority
} from '@/lib/db/models/notification';

interface NotificationPanelProps {
  userId: number;
  onClose: () => void;
  onNotificationRead: () => void;
}

export default function NotificationPanel({
  userId,
  onClose,
  onNotificationRead
}: NotificationPanelProps) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const data = await getNotifications(userId);
        setNotifications(data);
      } catch (error) {
        console.error('Failed to fetch notifications:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchNotifications();
  }, [userId]);

  const handleMarkAsRead = async (notificationId: number) => {
    try {
      await markNotificationAsRead(notificationId, userId);
      // Update local state
      setNotifications(notifications.map(notification =>
        notification.id === notificationId
          ? { ...notification, readAt: new Date() }
          : notification
      ));
      onNotificationRead();
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
    }
  };

  // Get priority styles
  const getPriorityStyles = (priority: string, readAt: Date | null) => {
    const baseStyles = "border-l-[3px] transition-colors duration-200";
    const readStyles = readAt ? "opacity-60" : "";

    switch (priority) {
      case NotificationPriority.HIGH:
        return cn(
          baseStyles,
          "border-l-red-500",
          "bg-red-50/50 dark:bg-red-950/20",
          "hover:bg-red-50/80 dark:hover:bg-red-950/30",
          readStyles
        );
      case NotificationPriority.MEDIUM:
        return cn(
          baseStyles,
          "border-l-yellow-500",
          "bg-amber-50/50 dark:bg-yellow-950/20",
          "hover:bg-amber-50/80 dark:hover:bg-yellow-950/30",
          readStyles
        );
      default:
        return cn(
          baseStyles,
          "border-l-blue-500",
          "bg-blue-50/50 dark:bg-blue-950/20",
          "hover:bg-blue-50/80 dark:hover:bg-blue-950/30",
          readStyles
        );
    }
  };

  // Get icon based on notification type
  const getTypeIcon = (type: string) => {
    switch (type) {
      case NotificationType.TRANSACTION:
        return '💰';
      case NotificationType.ANALYST_RECOMMENDATION:
        return '📊';
      case NotificationType.PORTFOLIO_CHANGE:
        return '📈';
      default:
        return 'ℹ️';
    }
  };

  return (
    <div className="w-96 overflow-hidden">
      <div className="p-4 border-b flex justify-between items-center bg-white dark:bg-card dark:border-border">
        <div className="flex items-center gap-2">
          <h3 className="font-semibold text-lg">Notifications</h3>
          {notifications.length > 0 && (
            <span className="text-sm text-muted-foreground">
              ({notifications.filter(n => !n.readAt).length} unread)
            </span>
          )}
        </div>
        <Button variant="ghost" size="icon" onClick={onClose} className="hover:bg-gray-100 dark:hover:bg-background/10">
          <X className="h-4 w-4" />
        </Button>
      </div>

      <ScrollArea className="h-[400px] bg-white dark:bg-card">
        {loading ? (
          <div className="p-4 text-center text-muted-foreground bg-white dark:bg-card">
            <div className="animate-spin h-6 w-6 border-2 border-primary border-t-transparent rounded-full mx-auto mb-2"></div>
            Loading notifications...
          </div>
        ) : notifications.length === 0 ? (
          <div className="p-8 text-center bg-white dark:bg-card">
            <div className="text-4xl mb-2">📬</div>
            <p className="text-muted-foreground">No notifications yet</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100 dark:divide-border bg-white dark:bg-card">
            {notifications.map((notification) => (
              <div
                key={notification.id}
                className={cn(
                  "relative",
                  getPriorityStyles(notification.priority, notification.readAt)
                )}
              >
                <div className="p-4">
                  <div className="flex justify-between items-start gap-2">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="flex-shrink-0">{getTypeIcon(notification.type)}</span>
                        <h4 className="font-medium text-gray-900 dark:text-foreground truncate">
                          {notification.title}
                        </h4>
                      </div>
                      <p className="text-sm text-gray-600 dark:text-muted-foreground line-clamp-2 mb-2">
                        {notification.message}
                      </p>
                      <div className="text-xs text-gray-500 dark:text-muted-foreground">
                        {formatDistanceToNow(new Date(notification.createdAt), { addSuffix: true })}
                      </div>
                    </div>
                    
                    {!notification.readAt && (
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleMarkAsRead(notification.id)}
                        className="flex-shrink-0 hover:bg-gray-100 dark:hover:bg-background/10"
                      >
                        <Check className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </ScrollArea>
    </div>
  );
} 