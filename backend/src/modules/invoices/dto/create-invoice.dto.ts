import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { InvoiceStatus } from '@prisma/client';
import { IsEnum, IsNotEmpty, IsNumber, IsOptional, IsString, Min } from 'class-validator';

export class CreateInvoiceDto {
  @ApiProperty({ example: 'INV-2026-001' })
  @IsString()
  @IsNotEmpty()
  invoiceNumber: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  customerId?: string;

  @ApiProperty({ enum: InvoiceStatus, default: InvoiceStatus.ISSUED })
  @IsEnum(InvoiceStatus)
  status: InvoiceStatus = InvoiceStatus.ISSUED;

  @ApiProperty({ example: 12500.0 })
  @IsNumber()
  @Min(0)
  totalAmount: number;

  @ApiPropertyOptional({ example: 2250.0 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  taxAmount?: number = 0;

  @ApiPropertyOptional({ example: 500.0 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  discountAmount?: number = 0;

  @ApiPropertyOptional({ example: 12500.0 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  paidAmount?: number = 0;
}
